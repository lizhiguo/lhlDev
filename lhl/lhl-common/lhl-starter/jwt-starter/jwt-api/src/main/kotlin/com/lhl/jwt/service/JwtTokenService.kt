package com.lhl.jwt.service

import com.lhl.jwt.config.JwtSettings
import com.lhl.jwt.domain.dto.PayloadDTO
import com.lhl.jwt.domain.vo.AuthTokenVO
import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.exception.JwtParseException
import com.lhl.jwt.exception.JwtSignatureVerifyException
import com.lhl.jwt.exception.JwtVerificationFailedException
import com.lhl.jwt.store.RefreshToken
import com.lhl.jwt.store.RefreshTokenStore
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.JwtUtils
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.text.ParseException
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * JWT 签发与校验的核心服务。
 *
 * 设计要点：
 * 1. 密钥来源于配置（JwtSettings.secret），并按用户派生，不同用户的 token 互不可伪造；
 * 2. 签发与校验都走 nimbus 的 SignedJWT，强制 HS256 + typ=JWT；
 * 3. 校验链路覆盖：算法、签名、exp、nbf、iss、aud、tokenType；
 * 4. 刷新令牌采用轮换（rotation）策略：每次刷新都会吊销旧的 refresh token。
 *
 * 该类不依赖 Spring 容器，可直接单测。
 */
class JwtTokenService(
    private val settings: JwtSettings,
    private val refreshTokenStore: RefreshTokenStore
) {

    private val log = KotlinLogging.logger {}

    init {
        require(settings.secret.length >= MIN_SECRET_LENGTH) {
            "jwt.security.secret 长度必须 >= $MIN_SECRET_LENGTH 个字符（HS256 要求 >= 256bit）"
        }
    }

    /** 当前配置快照 */
    val configuration: JwtSettings
        get() = settings

    // ------------------------------------------------------------------ 签发

    /**
     * 签发 access token。
     *
     * @param user 认证主体
     * @param extraClaims 额外声明；不要放入密码等敏感信息（JWT 只签名不加密）
     */
    fun createAccessToken(user: UserDetails, extraClaims: Map<String, Any?> = emptyMap()): String =
        createToken(
            user.username,
            user.authorities,
            settings.accessTokenExpiration,
            JwtConstant.TOKEN_TYPE_ACCESS,
            extraClaims
        )

    /**
     * 签发 refresh token，并登记到 RefreshTokenStore。
     */
    fun createRefreshToken(user: UserDetails, extraClaims: Map<String, Any?> = emptyMap()): String {
        val token = createToken(
            user.username,
            user.authorities,
            settings.refreshTokenExpiration,
            JwtConstant.TOKEN_TYPE_REFRESH,
            extraClaims
        )
        val payload = parse(token, null)
        val jti = payload.jti ?: throw JwtVerificationFailedException("refresh token 缺少 jti")
        val expiresAt = payload.exp ?: throw JwtVerificationFailedException("refresh token 缺少 exp")
        refreshTokenStore.save(RefreshToken(jti, user.username, expiresAt * 1000))
        return token
    }

    /**
     * 一次性签发 access token + refresh token。
     */
    fun createTokenPair(user: UserDetails, extraClaims: Map<String, Any?> = emptyMap()): AuthTokenVO =
        AuthTokenVO(
            accessToken = createAccessToken(user, extraClaims),
            refreshToken = createRefreshToken(user, extraClaims),
            tokenType = settings.tokenPrefix.trim().ifEmpty { "Bearer" },
            expiresIn = settings.accessTokenExpiration.seconds
        )

    private fun createToken(
        username: String,
        authorities: Collection<GrantedAuthority>,
        expiration: Duration,
        tokenType: String,
        extraClaims: Map<String, Any?>
    ): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .jwtID(UUID.randomUUID().toString())
            .subject(username)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plus(expiration)))
            .claim(JwtConstant.CLAIM_USERNAME, username)
            .claim(JwtConstant.CLAIM_TOKEN_TYPE, tokenType)
            .claim(JwtConstant.CLAIM_AUTHORITIES, authorities.map { it.authority })
        settings.issuer?.takeIf { it.isNotBlank() }?.let { claims.issuer(it) }
        settings.audience?.takeIf { it.isNotBlank() }?.let { claims.audience(it) }
        extraClaims.forEach { (key, value) -> value?.let { claims.claim(key, it) } }

        val header = JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build()
        val signedJwt = SignedJWT(header, claims.build())
        return try {
            signedJwt.sign(MACSigner(JwtUtils.deriveKey(settings.secret, username)))
            signedJwt.serialize()
        } catch (e: JOSEException) {
            throw JwtSignatureVerifyException("生成签名异常", e)
        }
    }

    // ------------------------------------------------------------------ 校验
    /**
     * 校验 token 并返回荷载。
     *
     * @param expectedTokenType 期望的 token 类型，传 null 表示不校验
     * @throws JwtParseException token 结构非法
     * @throws JwtSignatureVerifyException 签名不匹配
     * @throws JwtExpireException 已过期
     * @throws JwtVerificationFailedException iss / aud / nbf / tokenType 校验失败
     */
    fun parse(token: String, expectedTokenType: String? = JwtConstant.TOKEN_TYPE_ACCESS): PayloadDTO {
        if (token.isBlank()) {
            throw JwtParseException(JwtConstant.TOKEN_NOT_EMPTY)
        }
        val signedJwt = try {
            SignedJWT.parse(token)
        } catch (e: ParseException) {
            throw JwtParseException(JwtConstant.TOKEN_PARSE_ERROR, e)
        }
        if (signedJwt.header.algorithm != JWSAlgorithm.HS256) {
            throw JwtSignatureVerifyException("非法的签名算法: " + signedJwt.header.algorithm)
        }
        val claims = signedJwt.jwtClaimsSet
        val username = claims.subject
        if (username.isNullOrBlank()) {
            throw JwtParseException(JwtConstant.TOKEN_PARSE_ERROR)
        }
        val verified = try {
            signedJwt.verify(MACVerifier(JwtUtils.deriveKey(settings.secret, username)))
        } catch (e: JOSEException) {
            throw JwtSignatureVerifyException(JwtConstant.TOKEN_VERIFY_ERROR, e)
        }
        if (!verified) {
            throw JwtSignatureVerifyException(JwtConstant.TOKEN_VERIFY_ERROR)
        }
        validate(claims, expectedTokenType)
        return toPayload(claims)
    }

    /** 解析 token 获取用户名，失败返回 null */
    fun extractUsername(token: String, expectedTokenType: String? = JwtConstant.TOKEN_TYPE_ACCESS): String? =
        try {
            parse(token, expectedTokenType).sub
        } catch (e: Exception) {
            log.debug { "extract username failed: ${e.message}" }
            null
        }

    /** 不验签地读取声明中的用户名，仅用于推导每用户密钥，必须配合 parse 使用 */
    fun readSubjectUnverified(token: String): String? = try {
        SignedJWT.parse(token).jwtClaimsSet.subject
    } catch (e: ParseException) {
        null
    }

    /** token 距离过期的剩余时长；无法解析时返回 Duration.ZERO */
    fun remainingTtl(token: String): Duration {
        val exp = try {
            SignedJWT.parse(token).jwtClaimsSet.expirationTime?.time
        } catch (e: ParseException) {
            null
        } ?: return Duration.ZERO
        return Duration.ofMillis(JwtUtils.remainingMillis(exp))
    }

    /** 是否需要自动续期（距上次签发超过配置的间隔） */
    fun shouldAutoRefresh(payload: PayloadDTO): Boolean {
        if (!settings.tokenAutoRefresh) {
            return false
        }
        val issuedAt = payload.iat ?: return false
        val elapsed = Instant.now().epochSecond - issuedAt
        return elapsed >= settings.tokenRefreshInterval.seconds
    }

    // ------------------------------------------------------------------ 刷新

    /**
     * 使用 refresh token 换取新的令牌对（轮换：旧的 refresh token 立即失效）。
     */
    fun refresh(
        refreshToken: String,
        userDetailsService: JwtUserDetailsService,
        extraClaims: Map<String, Any?> = emptyMap()
    ): AuthTokenVO {
        val payload = parse(refreshToken, JwtConstant.TOKEN_TYPE_REFRESH)
        val jti = payload.jti ?: throw JwtVerificationFailedException("refresh token 缺少 jti")
        val username = payload.sub ?: throw JwtVerificationFailedException("refresh token 缺少 sub")
        if (refreshTokenStore.find(jti) == null) {
            throw JwtVerificationFailedException("refresh token 已失效，请重新登录")
        }
        val user = userDetailsService.loadUserByUsername(username)
        refreshTokenStore.revoke(jti)
        log.info { "rotate refresh token, user = $username" }
        return createTokenPair(user, extraClaims)
    }

    /** 吊销某个用户的全部 refresh token（登出时调用） */
    fun revokeRefreshTokens(username: String) {
        refreshTokenStore.revokeByUsername(username)
    }
    // ------------------------------------------------------------------ 内部

    private fun validate(claims: JWTClaimsSet, expectedTokenType: String?) {
        val now = Date()
        val expirationTime = claims.expirationTime
        if (expirationTime != null && !expirationTime.after(now)) {
            throw JwtExpireException(JwtConstant.TOKEN_EXPIRE)
        }
        val notBeforeTime = claims.notBeforeTime
        if (notBeforeTime != null && notBeforeTime.after(now)) {
            throw JwtVerificationFailedException("token 尚未生效")
        }
        settings.issuer?.takeIf { it.isNotBlank() }?.let {
            if (claims.issuer != it) {
                throw JwtVerificationFailedException("iss 不匹配")
            }
        }
        settings.audience?.takeIf { it.isNotBlank() }?.let {
            if (claims.audience?.contains(it) != true) {
                throw JwtVerificationFailedException("aud 不匹配")
            }
        }
        expectedTokenType?.let {
            if (claims.getClaim(JwtConstant.CLAIM_TOKEN_TYPE) != it) {
                throw JwtVerificationFailedException(JwtConstant.TOKEN_TYPE_MISMATCH)
            }
        }
    }

    private fun toPayload(claims: JWTClaimsSet): PayloadDTO = PayloadDTO(
        jti = claims.getJWTID(),
        iss = claims.issuer,
        exp = claims.expirationTime?.time?.div(1000),
        sub = claims.subject,
        aud = claims.audience?.firstOrNull(),
        nbf = claims.notBeforeTime?.time?.div(1000),
        iat = claims.issueTime?.time?.div(1000),
        username = claims.getClaim(JwtConstant.CLAIM_USERNAME) as? String,
        gender = claims.getClaim("gender") as? String,
        birthday = claims.getClaim("birthday") as? String,
        isAdmin = (claims.getClaim("isAdmin") as? String)?.toBoolean(),
        authorities = (claims.getClaim(JwtConstant.CLAIM_AUTHORITIES) as? List<*>)
            ?.mapNotNull { it?.toString() },
        tokenType = claims.getClaim(JwtConstant.CLAIM_TOKEN_TYPE) as? String
    )

    companion object {

        /** HS256 要求密钥 >= 256bit，即 >= 32 个字符 */
        const val MIN_SECRET_LENGTH: Int = 32
    }
}
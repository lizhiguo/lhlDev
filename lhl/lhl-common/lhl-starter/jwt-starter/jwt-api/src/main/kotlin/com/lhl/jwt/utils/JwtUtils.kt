package com.lhl.jwt.utils

import cn.hutool.v7.core.date.LocalDateTimeUtil
import cn.hutool.v7.crypto.SecureUtil
import cn.hutool.v7.json.JSONUtil
import com.lhl.jwt.domain.dto.PayloadDTO
import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.exception.JwtSignatureVerifyException
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.GrantedAuthority
import java.text.ParseException
import java.time.Instant
import java.util.UUID

object JwtUtils {

    private val log = KotlinLogging.logger {}

    const val TOKEN_SECRET = "king"
    private const val EXPIRATION = 120L

    @JvmStatic
    @Throws(JOSEException::class)
    fun createToken(
        username: String,
        authorities: Collection<GrantedAuthority>,
        secret: String
    ): String {
        val asList = authorities.map { it.authority }
        val payloadDTO = defaultPayloadDTO
        payloadDTO.sub = username
        payloadDTO.username = username
        payloadDTO.authorities = asList
        val payloadJsonString = JSONUtil.toJsonStr(payloadDTO)
        return createToken(payloadJsonString, secret)
    }

    @JvmStatic
    @Throws(JOSEException::class)
    fun createToken(payloadJsonString: String, secret: String): String {
        val jwsHeader = JWSHeader(JWSAlgorithm.HS256)
        val payload = Payload(payloadJsonString)
        val jwsObject = JWSObject(jwsHeader, payload)
        val jwsSigner = MACSigner(SecureUtil.md5(TOKEN_SECRET + secret))
        jwsObject.sign(jwsSigner)
        return jwsObject.serialize()
    }

    @JvmStatic
    @Throws(ParseException::class, JOSEException::class)
    fun getPayLoadByToken(token: String, secret: String): PayloadDTO {
        val jwsObject = JWSObject.parse(token)
        val jwsVerifier = MACVerifier(SecureUtil.md5(TOKEN_SECRET + secret))
        if (!jwsObject.verify(jwsVerifier)) {
            throw JwtSignatureVerifyException("签名认证失败!")
        }
        val payload = jwsObject.payload.toString()
        val payloadDto = JSONUtil.toBean(payload, PayloadDTO::class.java)
        val exp = payloadDto.exp ?: 0L
        if (exp < Instant.now().epochSecond) {
            throw JwtExpireException("token已过期！")
        }
        return payloadDto
    }

    val defaultPayloadDTO: PayloadDTO
        get() {
            val now = Instant.now()
            val exp = now.plusSeconds(EXPIRATION)
            log.debug { "now==${LocalDateTimeUtil.of(now)}" }
            log.debug { "now==${now.toEpochMilli()}" }
            return PayloadDTO(
                iat = now.epochSecond,
                exp = exp.epochSecond,
                jti = UUID.randomUUID().toString()
            )
        }
}
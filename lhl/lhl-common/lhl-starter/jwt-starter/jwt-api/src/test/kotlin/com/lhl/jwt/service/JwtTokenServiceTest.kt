package com.lhl.jwt.service

import com.lhl.jwt.config.JwtSettings
import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.exception.JwtSignatureVerifyException
import com.lhl.jwt.exception.JwtVerificationFailedException
import com.lhl.jwt.store.InMemoryRefreshTokenStore
import com.lhl.jwt.store.InMemoryTokenBlacklistStore
import com.lhl.jwt.store.TokenBlacklistStore
import com.lhl.jwt.utils.JwtUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.time.Duration

class JwtTokenServiceTest {

    private val secret = "unit-test-secret-key-should-be-long-enough-32"

    private val settings = JwtSettings(
        secret = secret,
        issuer = "lhl-test",
        audience = "lhl-client",
        accessTokenExpiration = Duration.ofHours(1),
        refreshTokenExpiration = Duration.ofDays(7)
    )

    private lateinit var refreshTokenStore: InMemoryRefreshTokenStore

    private lateinit var service: JwtTokenService

    private val user = User(
        "lhl",
        "encoded",
        listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("USER_READ"))
    )

    @BeforeEach
    fun setUp() {
        refreshTokenStore = InMemoryRefreshTokenStore()
        service = JwtTokenService(settings, refreshTokenStore)
    }

    @Test
    @DisplayName("签发的 access token 可以解析出用户与权限")
    fun shouldParseAccessToken() {
        val payload = service.parse(service.createAccessToken(user))

        assertThat(payload.sub).isEqualTo("lhl")
        assertThat(payload.username).isEqualTo("lhl")
        assertThat(payload.iss).isEqualTo("lhl-test")
        assertThat(payload.aud).isEqualTo("lhl-client")
        assertThat(payload.tokenType).isEqualTo("access")
        assertThat(payload.authorities).containsExactly("ROLE_USER", "USER_READ")
    }

    @Test
    @DisplayName("密钥过短时拒绝启动")
    fun shouldRejectShortSecret() {
        assertThatThrownBy { JwtTokenService(settings.copy(secret = "short"), refreshTokenStore) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @DisplayName("被篡改的 token 验签失败")
    fun shouldRejectTamperedToken() {
        val token = service.createAccessToken(user)
        val tampered = token.dropLast(2) + (if (token.endsWith("aa")) "bb" else "aa")

        assertThatThrownBy { service.parse(tampered) }
            .isInstanceOfAny(JwtSignatureVerifyException::class.java, JwtVerificationFailedException::class.java)
    }

    @Test
    @DisplayName("别的密钥签发的 token 无法通过校验")
    fun shouldRejectTokenSignedByOtherSecret() {
        val other = JwtTokenService(
            settings.copy(secret = "another-secret-key-that-is-long-enough-32"),
            refreshTokenStore
        )

        assertThatThrownBy { service.parse(other.createAccessToken(user)) }
            .isInstanceOf(JwtSignatureVerifyException::class.java)
    }

    @Test
    @DisplayName("过期 token 抛出过期异常")
    fun shouldRejectExpiredToken() {
        val shortLived = JwtTokenService(settings.copy(accessTokenExpiration = Duration.ofSeconds(-1)), refreshTokenStore)

        assertThatThrownBy { service.parse(shortLived.createAccessToken(user)) }
            .isInstanceOf(JwtExpireException::class.java)
    }
    @Test
    @DisplayName("iss / aud 不匹配时校验失败")
    fun shouldRejectWrongIssuerOrAudience() {
        val other = JwtTokenService(settings.copy(issuer = "other-issuer"), refreshTokenStore)
        assertThatThrownBy { other.parse(service.createAccessToken(user)) }
            .isInstanceOf(JwtVerificationFailedException::class.java)

        val otherAudience = JwtTokenService(settings.copy(audience = "other-client"), refreshTokenStore)
        assertThatThrownBy { otherAudience.parse(service.createAccessToken(user)) }
            .isInstanceOf(JwtVerificationFailedException::class.java)
    }

    @Test
    @DisplayName("refresh token 不能当作 access token 使用")
    fun shouldRejectRefreshTokenAsAccessToken() {
        val refreshToken = service.createRefreshToken(user)

        assertThatThrownBy { service.parse(refreshToken) }
            .isInstanceOf(JwtVerificationFailedException::class.java)
    }

    @Test
    @DisplayName("刷新令牌轮换：旧 refresh token 立即失效")
    fun shouldRotateRefreshToken() {
        val userDetailsService = object : JwtUserDetailsService {
            override fun loadUserByUsername(username: String): UserDetails = user
        }
        val first = service.createTokenPair(user)

        val second = service.refresh(first.refreshToken!!, userDetailsService)
        assertThat(second.accessToken).isNotEqualTo(first.accessToken)
        assertThat(second.refreshToken).isNotEqualTo(first.refreshToken)

        assertThatThrownBy { service.refresh(first.refreshToken!!, userDetailsService) }
            .isInstanceOf(JwtVerificationFailedException::class.java)
    }

    @Test
    @DisplayName("登出后 refresh token 一并吊销")
    fun shouldRevokeRefreshTokensOnLogout() {
        val tokenPair = service.createTokenPair(user)
        val payload = service.parse(tokenPair.refreshToken!!, null)
        assertThat(refreshTokenStore.find(payload.jti!!)).isNotNull

        service.revokeRefreshTokens("lhl")

        assertThat(refreshTokenStore.find(payload.jti!!)).isNull()
    }

    @Test
    @DisplayName("用户不存在时抛出 UsernameNotFoundException")
    fun shouldThrowWhenUserMissing() {
        val emptyService = object : JwtUserDetailsService {
            override fun loadUserByUsername(username: String): UserDetails = throw UsernameNotFoundException(username)
        }
        val pair = service.createTokenPair(user)

        assertThatThrownBy { service.refresh(pair.refreshToken!!, emptyService) }
            .isInstanceOf(UsernameNotFoundException::class.java)
    }

    @Test
    @DisplayName("空串与非法格式的 token 解析失败返回 null")
    fun shouldRejectMalformedToken() {
        assertThat(service.extractUsername("")).isNull()
        assertThat(service.extractUsername("not-a-jwt")).isNull()
        assertThat(service.extractUsername("a.b.c")).isNull()
    }

    @Test
    @DisplayName("剩余有效期可用于黑名单 TTL")
    fun shouldComputeRemainingTtl() {
        val token = service.createAccessToken(user)
        assertThat(service.remainingTtl(token)).isGreaterThan(Duration.ofMinutes(50))
        assertThat(service.remainingTtl("bad-token")).isEqualTo(Duration.ZERO)
    }

    @Test
    @DisplayName("黑名单吊销后立即失效，过期条目会被清理")
    fun blacklistShouldWork() {
        val store: TokenBlacklistStore = InMemoryTokenBlacklistStore(Duration.ofMinutes(1))
        assertThat(store.isBlacklisted("token-1")).isFalse()

        store.revoke("token-1", Duration.ofMinutes(5))
        assertThat(store.isBlacklisted("token-1")).isTrue()
        assertThat(store.size()).isEqualTo(1)

        store.revoke("token-2", Duration.ofMillis(1))
        Thread.sleep(20)
        store.removeExpired()
        assertThat(store.size()).isEqualTo(1)
    }

    @Test
    @DisplayName("每用户密钥互不相同")
    fun derivedKeysShouldDifferPerUser() {
        assertThat(JwtUtils.deriveKey(secret, "a")).isNotEqualTo(JwtUtils.deriveKey(secret, "b"))
        assertThat(JwtUtils.deriveKey(secret, "a")).isEqualTo(JwtUtils.deriveKey(secret, "a"))
    }

    @Test
    @DisplayName("自动续期开关生效")
    fun shouldAutoRefreshOnlyWhenEnabled() {
        assertThat(service.shouldAutoRefresh(service.parse(service.createAccessToken(user)))).isFalse()

        val refreshable = JwtTokenService(
            settings.copy(tokenRefreshInterval = Duration.ofSeconds(1)),
            refreshTokenStore
        )
        val payload = refreshable.parse(refreshable.createAccessToken(user))
        Thread.sleep(1600)
        assertThat(refreshable.shouldAutoRefresh(payload)).isTrue()
    }
}
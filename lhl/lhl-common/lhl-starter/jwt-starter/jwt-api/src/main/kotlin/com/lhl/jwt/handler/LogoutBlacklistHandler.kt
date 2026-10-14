package com.lhl.jwt.handler

import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.store.TokenBlacklistStore
import com.lhl.jwt.utils.JwtUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration

/**
 * 登出黑名单处理器：把当前 access token 加入黑名单，实现立即失效。
 *
 * TTL 取 token 的剩余有效期，黑名单不会无限增长。
 */
class LogoutBlacklistHandler(
    private val jwtTokenService: JwtTokenService,
    private val tokenBlacklistStore: TokenBlacklistStore
) : LogoutHandler {

    private val log = KotlinLogging.logger {}

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        val settings = jwtTokenService.configuration
        if (!settings.blacklistEnabled) {
            return
        }
        val token = JwtUtils.resolveToken(request, settings.header, settings.tokenPrefix, settings.cookieName)
        if (token.isNullOrBlank()) {
            log.debug { "logout without token, skip blacklisting" }
            return
        }
        val ttl = jwtTokenService.remainingTtl(token)
        if (ttl <= Duration.ZERO) {
            log.debug { "token already expired, skip blacklisting" }
            return
        }
        tokenBlacklistStore.revoke(token, ttl)
        log.info { "token blacklisted, ttl = ${ttl.seconds}s" }
    }
}
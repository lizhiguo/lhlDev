package com.lhl.jwt.handler

import com.lhl.jwt.service.JwtTokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserCache
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.cache.NullUserCache
import org.springframework.security.web.authentication.logout.LogoutHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * 登出清理处理器：清除用户缓存，并吊销该用户全部 refresh token，
 * 避免登出后仍能用 refresh token 换到新的 access token。
 */
class TokenClearLogoutHandler(
    private val jwtTokenService: JwtTokenService
) : LogoutHandler {

    private val log = KotlinLogging.logger {}

    private var userCache: UserCache = NullUserCache()

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        val username = (authentication?.principal as? UserDetails)?.username
        if (username.isNullOrBlank()) {
            return
        }
        userCache.removeUserFromCache(username)
        jwtTokenService.revokeRefreshTokens(username)
        log.info { "logout success, refresh tokens of $username revoked" }
    }

    fun setUserCache(userCache: UserCache) {
        this.userCache = userCache
    }
}
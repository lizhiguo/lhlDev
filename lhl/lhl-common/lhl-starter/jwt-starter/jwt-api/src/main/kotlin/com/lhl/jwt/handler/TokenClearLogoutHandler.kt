package com.lhl.jwt.handler

import com.lhl.jwt.service.JwtUserDetailsService
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserCache
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.cache.NullUserCache
import org.springframework.security.web.authentication.logout.LogoutHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class TokenClearLogoutHandler(
    private val jwtUserDetailsService: JwtUserDetailsService
) : LogoutHandler {

    private var userCache: UserCache = NullUserCache()

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        clearToken(authentication)
    }

    private fun clearToken(authentication: Authentication?) {
        if (authentication == null) return
        val user = authentication.principal as? UserDetails ?: return
        userCache.removeUserFromCache(user.username)
    }

    fun setUserCache(userCache: UserCache) {
        this.userCache = userCache
    }
}
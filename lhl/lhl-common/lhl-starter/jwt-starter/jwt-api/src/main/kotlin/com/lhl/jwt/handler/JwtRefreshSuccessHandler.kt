package com.lhl.jwt.handler

import com.lhl.jwt.exception.JwtSignatureVerifyException
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.JwtUtils
import com.nimbusds.jose.JOSEException
import com.nimbusds.jwt.SignedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserCache
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.cache.NullUserCache
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class JwtRefreshSuccessHandler(
    private val jwtUserDetailsService: JwtUserDetailsService
) : AuthenticationSuccessHandler {

    private val log = KotlinLogging.logger {}

    var tokenRefreshInterval: Int = 0
    private var userCache: UserCache = NullUserCache()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val token = (authentication as JwtAuthenticationToken).token ?: return
        log.debug { "jwt==$token" }
        try {
            val signedJwt = SignedJWT.parse(token)
            if (shouldTokenRefresh(signedJwt)) {
                val user = authentication.principal as UserDetails
                userCache.putUserInCache(user)
                val newToken = generateToken(user, user.username)
                response.setHeader(JwtConstant.AUTHORIZATION_HEADER, JwtConstant.AUTHORIZATION_START_STRING + newToken)
                response.setHeader(JwtConstant.ACCESS_CONTROL_EXPOSE_HEADER, JwtConstant.AUTHORIZATION_HEADER)
                log.info { "token refresh, new token : $newToken" }
            }
        } catch (e: ParseException) {
            throw JwtSignatureVerifyException("生成TOKEN异常")
        }
    }

    private fun shouldTokenRefresh(signedJwt: SignedJWT): Boolean {
        if (tokenRefreshInterval == 0) return false
        return try {
            val issueAt = signedJwt.jwtClaimsSet.issueTime
            val issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault())
            LocalDateTime.now().minusSeconds(tokenRefreshInterval.toLong()).isAfter(issueTime)
        } catch (e: ParseException) {
            false
        }
    }

    private fun generateToken(user: UserDetails, salt: String): String {
        return try {
            JwtUtils.createToken(user.username, user.authorities, salt)
        } catch (e: JOSEException) {
            throw JwtSignatureVerifyException("生成TOKEN异常")
        }
    }

    fun setUserCache(userCache: UserCache) {
        this.userCache = userCache
    }
}
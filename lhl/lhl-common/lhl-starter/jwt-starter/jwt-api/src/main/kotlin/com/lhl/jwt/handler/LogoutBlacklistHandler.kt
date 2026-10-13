package com.lhl.jwt.handler

import com.lhl.jwt.exception.JwtParseException
import com.lhl.jwt.utils.CacheManager
import com.lhl.jwt.utils.JwtConstant
import com.nimbusds.jwt.SignedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.text.ParseException
import java.util.Date

class LogoutBlacklistHandler : LogoutHandler {

    private val log = KotlinLogging.logger {}

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val jwt = getJwtToken(request)
        CacheManager.setData(jwt.parsedString, jwt, extractExpireTime(jwt))
    }

    private fun extractExpireTime(jwt: SignedJWT): Int {
        return try {
            val expiresAt = jwt.jwtClaimsSet.expirationTime
            if (!expiresAt.before(Date())) {
                (expiresAt.time - System.currentTimeMillis()).toInt()
            } else 0
        } catch (e: ParseException) {
            throw JwtParseException(JwtConstant.TOKEN_NOT_EMPTY)
        }
    }

    private fun getJwtToken(request: HttpServletRequest): SignedJWT {
        val authInfo = request.getHeader(JwtConstant.AUTHORIZATION_HEADER)
        if (authInfo.isNullOrBlank()) {
            throw BadCredentialsException(JwtConstant.TOKEN_NOT_EMPTY)
        }
        return try {
            SignedJWT.parse(authInfo.removePrefix(JwtConstant.AUTHORIZATION_START_STRING))
        } catch (e: ParseException) {
            throw JwtParseException(JwtConstant.TOKEN_NOT_EMPTY)
        }
    }
}
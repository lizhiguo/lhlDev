package com.lhl.jwt.security.providers

import com.lhl.jwt.exception.JwtException
import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.utils.JwtConstant
import com.nimbusds.jwt.SignedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import java.text.ParseException
import java.util.Date

class JwtAuthenticationProvider(
    private val userDetailsService: JwtUserDetailsService
) : AuthenticationProvider, InitializingBean {

    private val log = KotlinLogging.logger {}

    override fun afterPropertiesSet() {
        checkNotNull(userDetailsService) { "A UserDetailsService must be set" }
    }

    override fun authenticate(authentication: Authentication): Authentication {
        val jwt = (authentication as JwtAuthenticationToken).token
        checkNotNull(jwt) { JwtConstant.TOKEN_NOT_EMPTY }
        log.debug { "jwt==$jwt" }
        return try {
            val signedJwt = SignedJWT.parse(jwt)
            if (signedJwt.jwtClaimsSet.expirationTime.before(Date())) {
                throw JwtExpireException(JwtConstant.TOKEN_EXPIRE)
            }
            val username = signedJwt.jwtClaimsSet.subject
            val user = userDetailsService.loadUserByUsername(username)
                ?: return null
            JwtAuthenticationToken(user, jwt, user.authorities)
        } catch (e: ParseException) {
            throw JwtException(JwtConstant.TOKEN_VERIFY_ERROR)
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
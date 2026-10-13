package com.lhl.jwt.security.providers

import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.token.MobileCodeAuthenticationToken
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

class MobileCodeAuthenticationProvider(
    private val userDetailsService: JwtUserDetailsService
) : AuthenticationProvider, InitializingBean {

    override fun afterPropertiesSet() {
        checkNotNull(userDetailsService) { "A UserDetailsService must be set" }
    }

    override fun authenticate(authentication: Authentication): Authentication {
        require(authentication is MobileCodeAuthenticationToken) {
            "Only MobileCodeAuthenticationToken is supported"
        }
        val userDetails = userDetailsService.loadUserByMobile(authentication.principal.toString())
            ?: throw BadCredentialsException("User not found for mobile: ${authentication.principal}")
        return JwtAuthenticationToken(userDetails, null, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return MobileCodeAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
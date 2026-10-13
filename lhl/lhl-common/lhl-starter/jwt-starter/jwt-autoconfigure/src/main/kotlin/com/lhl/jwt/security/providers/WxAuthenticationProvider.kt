package com.lhl.jwt.security.providers

import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.token.WxAuthenticationToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

class WxAuthenticationProvider(
    private val userDetailsService: JwtUserDetailsService
) : AuthenticationProvider, InitializingBean {

    private val log = KotlinLogging.logger {}

    override fun afterPropertiesSet() {
        checkNotNull(userDetailsService) { "A UserDetailsService must be set" }
    }

    override fun authenticate(authentication: Authentication): Authentication {
        require(authentication is WxAuthenticationToken) {
            "Only WxAuthenticationToken is supported"
        }
        val userDetails = when (authentication.type) {
            "cp" -> userDetailsService.loadUserByMobile(authentication.mobile)
            "mp", "miniApp" -> userDetailsService.loadUserByOpenId(authentication.wxId)
            else -> throw BadCredentialsException("Unknown wx type: ${authentication.type}")
        }
        return JwtAuthenticationToken(userDetails!!, null, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return WxAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
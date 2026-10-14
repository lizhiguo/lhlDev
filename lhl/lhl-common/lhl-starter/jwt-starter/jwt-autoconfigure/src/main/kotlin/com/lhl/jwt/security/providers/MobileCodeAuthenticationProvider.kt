package com.lhl.jwt.security.providers

import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.service.MobileCodeValidator
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.token.MobileCodeAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

/**
 * 手机号 + 验证码认证 Provider：先校验验证码，再查询用户。
 */
class MobileCodeAuthenticationProvider(
    private val userDetailsService: JwtUserDetailsService,
    private val mobileCodeValidator: MobileCodeValidator
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication as MobileCodeAuthenticationToken
        val mobile = token.principal.toString()
        val code = token.credentials.orEmpty()
        if (!mobileCodeValidator.isValid(mobile, code)) {
            throw BadCredentialsException("验证码错误或已过期")
        }
        val userDetails = userDetailsService.loadUserByMobile(mobile)
        return JwtAuthenticationToken(userDetails, null, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean =
        MobileCodeAuthenticationToken::class.java.isAssignableFrom(authentication)
}
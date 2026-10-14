package com.lhl.jwt.security.providers

import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.token.WxAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

/**
 * 微信登录认证 Provider：
 * - `cp`（企业微信）按手机号查用户；
 * - `mp` / `miniApp`（公众号 / 小程序）按 openId 查用户。
 */
class WxAuthenticationProvider(
    private val userDetailsService: JwtUserDetailsService
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication as WxAuthenticationToken
        val userDetails = when (token.type) {
            TYPE_CP -> {
                val mobile = token.mobile?.takeIf { it.isNotBlank() }
                    ?: throw BadCredentialsException("企业微信登录必须携带 mobile")
                userDetailsService.loadUserByMobile(mobile)
            }

            TYPE_MP, TYPE_MINI_APP -> userDetailsService.loadUserByOpenId(token.wxId)

            else -> throw BadCredentialsException("不支持的微信登录类型: ${token.type}")
        }
        return JwtAuthenticationToken(userDetails, null, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean =
        WxAuthenticationToken::class.java.isAssignableFrom(authentication)

    companion object {

        const val TYPE_CP: String = "cp"
        const val TYPE_MP: String = "mp"
        const val TYPE_MINI_APP: String = "miniApp"
    }
}
package com.lhl.jwt.security.configs

import com.lhl.jwt.security.filters.WxAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

/**
 * 注册微信登录过滤器。
 */
class WxConfigurer<B : HttpSecurityBuilder<B>>(
    private val wxAuthenticationFilter: WxAuthenticationFilter
) : AbstractHttpConfigurer<WxConfigurer<B>, B>() {

    private var authenticationManager: AuthenticationManager? = null

    override fun configure(builder: B) {
        (authenticationManager ?: builder.getSharedObject(AuthenticationManager::class.java))
            ?.let { wxAuthenticationFilter.setAuthenticationManager(it) }
        builder.addFilterBefore(wxAuthenticationFilter, LogoutFilter::class.java)
    }

    fun authenticationManager(authenticationManager: AuthenticationManager): WxConfigurer<B> {
        this.authenticationManager = authenticationManager
        return this
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): WxConfigurer<B> {
        wxAuthenticationFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: AuthenticationFailureHandler): WxConfigurer<B> {
        wxAuthenticationFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
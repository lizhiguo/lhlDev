package com.lhl.jwt.security.configs

import com.lhl.jwt.handler.JwtAuthenticationFailureHandler
import com.lhl.jwt.security.filters.WxAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

class WxConfigurer<T : WxConfigurer<T, B>, B : HttpSecurityBuilder<B>> :
    AbstractHttpConfigurer<T, B>() {

    private val wxAuthenticationFilter = WxAuthenticationFilter()

    override fun configure(builder: B) {
        wxAuthenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager::class.java))
        builder.addFilterAfter(wxAuthenticationFilter, LogoutFilter::class.java)
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): WxConfigurer<T, B> {
        wxAuthenticationFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: JwtAuthenticationFailureHandler): WxConfigurer<T, B> {
        wxAuthenticationFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
package com.lhl.jwt.security.configs

import com.lhl.jwt.security.filters.JwtAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

class JwtAuthenticationConfigurer<T : JwtAuthenticationConfigurer<T, B>, B : HttpSecurityBuilder<B>> :
    AbstractHttpConfigurer<T, B>() {

    private val authFilter = JwtAuthenticationFilter()

    override fun configure(builder: B) {
        authFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager::class.java))
        builder.addFilterAfter(authFilter, LogoutFilter::class.java)
    }

    fun permissiveRequestUrls(vararg urls: String): JwtAuthenticationConfigurer<T, B> {
        authFilter.setPermissiveUrl(*urls)
        return this
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): JwtAuthenticationConfigurer<T, B> {
        authFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: com.lhl.jwt.handler.JwtAuthenticationFailureHandler): JwtAuthenticationConfigurer<T, B> {
        authFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
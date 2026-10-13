package com.lhl.jwt.security.configs

import com.lhl.jwt.handler.JwtAuthenticationFailureHandler
import com.lhl.jwt.security.filters.LoginAuthenticationFilter
import com.lhl.jwt.service.JwtUserDetailsService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

class LoginConfigurer<T : LoginConfigurer<T, B>, B : HttpSecurityBuilder<B>>(
    private val jwtUserDetailsService: JwtUserDetailsService
) : AbstractHttpConfigurer<T, B>() {

    private val loginAuthenticationFilter = LoginAuthenticationFilter()

    override fun configure(builder: B) {
        loginAuthenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager::class.java))
        loginAuthenticationFilter.setJwtUserDetailsService(jwtUserDetailsService)
        builder.addFilterBefore(loginAuthenticationFilter, LogoutFilter::class.java)
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): LoginConfigurer<T, B> {
        loginAuthenticationFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: JwtAuthenticationFailureHandler): LoginConfigurer<T, B> {
        loginAuthenticationFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
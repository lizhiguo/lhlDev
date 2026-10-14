package com.lhl.jwt.security.configs

import com.lhl.jwt.security.filters.LoginAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

/**
 * 注册账号密码登录过滤器。
 */
class LoginConfigurer<B : HttpSecurityBuilder<B>>(
    private val loginAuthenticationFilter: LoginAuthenticationFilter
) : AbstractHttpConfigurer<LoginConfigurer<B>, B>() {

    private var authenticationManager: AuthenticationManager? = null

    override fun configure(builder: B) {
        (authenticationManager ?: builder.getSharedObject(AuthenticationManager::class.java))
            ?.let { loginAuthenticationFilter.setAuthenticationManager(it) }
        builder.addFilterBefore(loginAuthenticationFilter, LogoutFilter::class.java)
    }

    fun authenticationManager(authenticationManager: AuthenticationManager): LoginConfigurer<B> {
        this.authenticationManager = authenticationManager
        return this
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): LoginConfigurer<B> {
        loginAuthenticationFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: AuthenticationFailureHandler): LoginConfigurer<B> {
        loginAuthenticationFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
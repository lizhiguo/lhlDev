package com.lhl.jwt.security.configs

import com.lhl.jwt.security.filters.MobileCodeAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

/**
 * 注册手机号验证码登录过滤器。
 */
class MobileConfigurer<B : HttpSecurityBuilder<B>>(
    private val mobileCodeAuthenticationFilter: MobileCodeAuthenticationFilter
) : AbstractHttpConfigurer<MobileConfigurer<B>, B>() {

    private var authenticationManager: AuthenticationManager? = null

    override fun configure(builder: B) {
        (authenticationManager ?: builder.getSharedObject(AuthenticationManager::class.java))
            ?.let { mobileCodeAuthenticationFilter.setAuthenticationManager(it) }
        builder.addFilterBefore(mobileCodeAuthenticationFilter, LogoutFilter::class.java)
    }

    fun authenticationManager(authenticationManager: AuthenticationManager): MobileConfigurer<B> {
        this.authenticationManager = authenticationManager
        return this
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): MobileConfigurer<B> {
        mobileCodeAuthenticationFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: AuthenticationFailureHandler): MobileConfigurer<B> {
        mobileCodeAuthenticationFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
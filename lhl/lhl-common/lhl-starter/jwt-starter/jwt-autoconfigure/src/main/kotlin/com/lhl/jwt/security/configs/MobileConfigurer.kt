package com.lhl.jwt.security.configs

import com.lhl.jwt.handler.JwtAuthenticationFailureHandler
import com.lhl.jwt.security.filters.MobileCodeAuthenticationFilter
import com.lhl.jwt.service.JwtUserDetailsService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

class MobileConfigurer<T : MobileConfigurer<T, B>, B : HttpSecurityBuilder<B>>(
    private val jwtUserDetailsService: JwtUserDetailsService
) : AbstractHttpConfigurer<T, B>() {

    private val filter = MobileCodeAuthenticationFilter()

    override fun configure(builder: B) {
        filter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager::class.java))
        filter.setJwtUserDetailsService(jwtUserDetailsService)
        builder.addFilterBefore(filter, LogoutFilter::class.java)
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): MobileConfigurer<T, B> {
        filter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: JwtAuthenticationFailureHandler): MobileConfigurer<T, B> {
        filter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
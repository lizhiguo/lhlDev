package com.lhl.jwt.security.configs

import com.lhl.jwt.security.AntPatternRequestMatcher
import com.lhl.jwt.security.filters.JwtAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * 把 [JwtAuthenticationFilter] 插入到 LogoutFilter 之后（先登出、再认证）。
 */
class JwtAuthenticationConfigurer<B : HttpSecurityBuilder<B>>(
    private val authFilter: JwtAuthenticationFilter
) : AbstractHttpConfigurer<JwtAuthenticationConfigurer<B>, B>() {

    private var authenticationManager: AuthenticationManager? = null

    private var permissiveUrls: List<String> = emptyList()

    private var extraMatchers: List<RequestMatcher> = emptyList()

    override fun configure(builder: B) {
        (authenticationManager ?: builder.getSharedObject(AuthenticationManager::class.java))
            ?.let { authFilter.setAuthenticationManager(it) }
        val matchers = permissiveUrls
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { AntPatternRequestMatcher(it) } + extraMatchers
        if (matchers.isNotEmpty()) {
            authFilter.setPermissiveRequestMatchers(matchers)
        }
        builder.addFilterAfter(authFilter, LogoutFilter::class.java)
    }

    /**
     * 指定使用的 AuthenticationManager。
     *
     * 建议传入 `AuthenticationConfiguration#getAuthenticationManager()`，
     * 它会收集容器中所有 AuthenticationProvider Bean。
     */
    fun authenticationManager(authenticationManager: AuthenticationManager): JwtAuthenticationConfigurer<B> {
        this.authenticationManager = authenticationManager
        return this
    }

    fun permissiveRequestUrls(vararg urls: String): JwtAuthenticationConfigurer<B> {
        permissiveUrls = urls.toList()
        return this
    }

    fun permissiveRequestMatchers(matchers: List<RequestMatcher>): JwtAuthenticationConfigurer<B> {
        extraMatchers = matchers
        return this
    }

    fun authenticationSuccessHandler(successHandler: AuthenticationSuccessHandler): JwtAuthenticationConfigurer<B> {
        authFilter.setAuthenticationSuccessHandler(successHandler)
        return this
    }

    fun authenticationFailureHandler(failureHandler: AuthenticationFailureHandler): JwtAuthenticationConfigurer<B> {
        authFilter.setAuthenticationFailureHandler(failureHandler)
        return this
    }
}
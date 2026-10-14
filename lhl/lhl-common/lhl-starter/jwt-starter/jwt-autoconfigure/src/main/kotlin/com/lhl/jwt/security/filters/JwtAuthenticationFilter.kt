package com.lhl.jwt.security.filters

import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.security.AntPatternRequestMatcher
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.store.TokenBlacklistStore
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.utils.JwtUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * JWT 认证过滤器：校验 token 并写入 SecurityContext。
 *
 * 关键行为：
 * 1. 请求未携带 token（请求头 / Cookie）时直接放行，由 `authorizeHttpRequests` 决定是否需要认证；
 * 2. 白名单路径不做认证；
 * 3. 命中黑名单按 token 过期处理，实现登出后立即失效；
 * 4. 任何认证异常都交给 failureHandler 输出 401，不再向下执行。
 */
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val tokenBlacklistStore: TokenBlacklistStore
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    private var authenticationManager: AuthenticationManager? = null

    private var successHandler: AuthenticationSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler()

    private var failureHandler: AuthenticationFailureHandler = SimpleUrlAuthenticationFailureHandler()

    private var permissiveRequestMatchers: List<RequestMatcher> = emptyList()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (isPermissiveRequest(request)) {
            return true
        }
        return resolveToken(request).isNullOrBlank()
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val manager = authenticationManager
        if (manager == null) {
            filterChain.doFilter(request, response)
            return
        }
        val token = resolveToken(request)
        if (token.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }
        try {
            if (tokenBlacklistStore.isBlacklisted(token)) {
                throw JwtExpireException()
            }
            val authentication: Authentication = manager.authenticate(JwtAuthenticationToken(token))
            SecurityContextHolder.getContext().authentication = authentication
            successHandler.onAuthenticationSuccess(request, response, authentication)
        } catch (e: AuthenticationException) {
            log.debug { "jwt authentication failed: ${e.message}" }
            SecurityContextHolder.clearContext()
            failureHandler.onAuthenticationFailure(request, response, e)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val settings = jwtTokenService.configuration
        return JwtUtils.resolveToken(request, settings.header, settings.tokenPrefix, settings.cookieName)
    }

    private fun isPermissiveRequest(request: HttpServletRequest): Boolean =
        permissiveRequestMatchers.any { it.matches(request) }

    fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
        this.authenticationManager = authenticationManager
    }

    fun setPermissiveUrl(vararg urls: String) {
        permissiveRequestMatchers = urls
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { AntPatternRequestMatcher(it) }
    }

    fun setPermissiveRequestMatchers(matchers: List<RequestMatcher>) {
        permissiveRequestMatchers = matchers
    }

    fun setAuthenticationSuccessHandler(successHandler: AuthenticationSuccessHandler) {
        this.successHandler = successHandler
    }

    fun setAuthenticationFailureHandler(failureHandler: AuthenticationFailureHandler) {
        this.failureHandler = failureHandler
    }
}
package com.lhl.jwt.security.filters

import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.utils.CacheManager
import com.lhl.jwt.utils.JwtConstant
import com.nimbusds.jwt.SignedJWT
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.text.ParseException

class JwtAuthenticationFilter : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    private val requiresAuthenticationRequestMatcher: RequestMatcher =
        RequestHeaderRequestMatcher(JwtConstant.AUTHORIZATION_HEADER)
    private var permissiveRequestMatchers: MutableList<RequestMatcher>? = null
    private var authenticationManager: AuthenticationManager? = null
    private var successHandler: AuthenticationSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler()
    private var failureHandler: AuthenticationFailureHandler = SimpleUrlAuthenticationFailureHandler()

    override fun afterPropertiesSet() {
        checkNotNull(authenticationManager) { "AuthenticationManager must be specified" }
        checkNotNull(successHandler) { "AuthenticationSuccessHandler must be specified" }
        checkNotNull(failureHandler) { "AuthenticationFailureHandler must be specified" }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.debug { "请求url==${request.requestURI}" }

        if (permissiveRequest(request)) {
            filterChain.doFilter(request, response)
            return
        }

        var authResult: Authentication? = null
        var failed: AuthenticationException? = null

        try {
            if (checkIsBlacklist(request)) {
                failed = JwtExpireException(JwtConstant.TOKEN_EXPIRE)
            } else {
                val token = getJwtToken(request)
                if (token.isNotEmpty()) {
                    val authToken = JwtAuthenticationToken(SignedJWT.parse(token))
                    authResult = authenticationManager?.authenticate(authToken)
                } else {
                    failed = BadCredentialsException(JwtConstant.TOKEN_NOT_EMPTY)
                }
            }
        } catch (e: ParseException) {
            failed = BadCredentialsException(JwtConstant.TOKEN_FORMAT_ERROR, e)
        } catch (e: AuthenticationException) {
            failed = e
        }

        if (authResult != null) {
            successfulAuthentication(request, response, filterChain, authResult)
        } else if (!permissiveRequest(request)) {
            unsuccessfulAuthentication(request, response, failed)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtToken(request: HttpServletRequest): String {
        val authInfo = request.getHeader(JwtConstant.AUTHORIZATION_HEADER)
        require(!authInfo.isNullOrBlank()) { JwtConstant.TOKEN_NOT_EMPTY }
        return authInfo.removePrefix(JwtConstant.AUTHORIZATION_START_STRING)
    }

    private fun checkIsBlacklist(request: HttpServletRequest): Boolean {
        val token = getJwtToken(request)
        return CacheManager.getData<Any>(token) != null
    }

    private fun unsuccessfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        failed: AuthenticationException?
    ) {
        SecurityContextHolder.clearContext()
        failureHandler.onAuthenticationFailure(request, response, failed)
    }

    private fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {
        SecurityContextHolder.getContext().authentication = authResult
        successHandler.onAuthenticationSuccess(request, response, authResult)
    }

    fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
        this.authenticationManager = authenticationManager
    }

    private fun permissiveRequest(request: HttpServletRequest): Boolean {
        return permissiveRequestMatchers?.any { it.matches(request) } ?: false
    }

    fun setPermissiveUrl(vararg urls: String) {
        val matchers = permissiveRequestMatchers ?: mutableListOf<RequestMatcher>().also { permissiveRequestMatchers = it }
        for (url in urls) {
            matchers.add(AntPathRequestMatcher(url.trim()))
        }
    }

    fun setAuthenticationSuccessHandler(successHandler: AuthenticationSuccessHandler) {
        checkNotNull(successHandler) { "successHandler cannot be null" }
        this.successHandler = successHandler
    }

    fun setAuthenticationFailureHandler(failureHandler: AuthenticationFailureHandler) {
        checkNotNull(failureHandler) { "failureHandler cannot be null" }
        this.failureHandler = failureHandler
    }
}
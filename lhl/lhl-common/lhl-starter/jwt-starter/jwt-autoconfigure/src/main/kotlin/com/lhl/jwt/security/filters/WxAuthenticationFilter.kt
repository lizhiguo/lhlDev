package com.lhl.jwt.security.filters

import com.lhl.jwt.exception.JwtParamException
import com.lhl.jwt.token.WxAuthenticationToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class WxAuthenticationFilter : AbstractAuthenticationProcessingFilter(
    AntPathRequestMatcher("/wxLogin", "POST")
) {

    private val log = KotlinLogging.logger {}

    override fun afterPropertiesSet() {
        checkNotNull(authenticationManager) { "AuthenticationManager must be specified" }
        checkNotNull(successHandler) { "AuthenticationSuccessHandler must be specified" }
        checkNotNull(failureHandler) { "AuthenticationFailureHandler must be specified" }
    }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val wxId = request.getParameter("wxId")
        require(!wxId.isNullOrBlank()) { "wxId must not be empty" }
        val mobile = request.getParameter("mobile")
        val type = request.getParameter("type")
        val authRequest: AbstractAuthenticationToken = WxAuthenticationToken(wxId, mobile, type)
        return authenticationManager!!.authenticate(authRequest)
    }
}
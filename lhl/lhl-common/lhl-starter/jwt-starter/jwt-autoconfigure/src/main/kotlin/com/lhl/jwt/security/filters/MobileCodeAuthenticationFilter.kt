package com.lhl.jwt.security.filters

import cn.hutool.v7.core.util.StrUtil
import cn.hutool.v7.json.JSONObject
import cn.hutool.v7.json.JSONUtil
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.MobileCodeAuthenticationToken
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.util.StreamUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.Charset

class MobileCodeAuthenticationFilter : AbstractAuthenticationProcessingFilter(
    AntPathRequestMatcher("/mobileCodeLogin", "POST")
) {

    companion object {
        const val SPRING_SECURITY_FORM_MOBILE_KEY = "mobile"
        const val SPRING_SECURITY_FORM_CODE_KEY = "code"
    }

    private var mobileParameter = SPRING_SECURITY_FORM_MOBILE_KEY
    private var codeParameter = SPRING_SECURITY_FORM_CODE_KEY
    private var jwtUserDetailsService: JwtUserDetailsService? = null

    override fun afterPropertiesSet() {
        checkNotNull(authenticationManager) { "AuthenticationManager must be specified" }
        checkNotNull(successHandler) { "AuthenticationSuccessHandler must be specified" }
        checkNotNull(failureHandler) { "AuthenticationFailureHandler must be specified" }
    }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val body = StreamUtils.copyToString(request.inputStream, Charset.forName("UTF-8"))

        val (mobile, code) = if (StrUtil.isBlank(body)) {
            request.getParameter(mobileParameter).orEmpty() to
                request.getParameter(codeParameter).orEmpty()
        } else {
            val jsonObj = JSONUtil.toBean(body.trim(), JSONObject::class.java)
            jsonObj.getStr(mobileParameter).orEmpty() to
                jsonObj.getStr(codeParameter).orEmpty()
        }

        val authRequest: AbstractAuthenticationToken = MobileCodeAuthenticationToken(mobile.trim(), code.trim())
        authRequest.details = authenticationDetailsSource.buildDetails(request)
        return authenticationManager!!.authenticate(authRequest)
    }

    fun getJwtUserDetailsService(): JwtUserDetailsService? = jwtUserDetailsService

    fun setJwtUserDetailsService(jwtUserDetailsService: JwtUserDetailsService) {
        this.jwtUserDetailsService = jwtUserDetailsService
    }
}
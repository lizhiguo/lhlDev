package com.lhl.jwt.security.filters

import cn.hutool.v7.core.util.StrUtil
import cn.hutool.v7.json.JSONObject
import cn.hutool.v7.json.JSONUtil
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.utils.JwtConstant
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.util.StreamUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.Charset

class LoginAuthenticationFilter : AbstractAuthenticationProcessingFilter(
    AntPathRequestMatcher("/**/login", "POST")
) {

    private var usernameParameter = JwtConstant.SPRING_SECURITY_FORM_USERNAME_KEY
    private var passwordParameter = JwtConstant.SPRING_SECURITY_FORM_PASSWORD_KEY
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

        val (username, password) = if (StrUtil.isBlank(body)) {
            request.getParameter(usernameParameter).orEmpty() to
                request.getParameter(passwordParameter).orEmpty()
        } else {
            obtainUsername(body).orEmpty() to obtainPassword(body).orEmpty()
        }

        val authRequest = UsernamePasswordAuthenticationToken(username.trim(), password)
        return authenticationManager!!.authenticate(authRequest)
    }

    private fun obtainPassword(body: String) = getStringFromRequest(body, passwordParameter)

    private fun obtainUsername(body: String) = getStringFromRequest(body, usernameParameter)

    private fun getStringFromRequest(body: String, key: String): String? {
        return body.takeIf { it.isNotBlank() }?.let {
            JSONUtil.toBean(it.trim(), JSONObject::class.java).getStr(key)
        }
    }

    fun getJwtUserDetailsService(): JwtUserDetailsService? = jwtUserDetailsService

    fun setJwtUserDetailsService(jwtUserDetailsService: JwtUserDetailsService) {
        this.jwtUserDetailsService = jwtUserDetailsService
    }
}
package com.lhl.jwt.security.filters

import org.springframework.security.authentication.BadCredentialsException
import com.lhl.jwt.security.AntPatternRequestMatcher
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.RequestBodyUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.util.StreamUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

/**
 * 账号密码登录过滤器：支持 application/json 请求体或表单参数。
 *
 * 用户名 / 密码字段名可通过 [usernameParameter] / [passwordParameter] 调整。
 */
class LoginAuthenticationFilter(loginPath: String) : AbstractAuthenticationProcessingFilter(
    AntPatternRequestMatcher(loginPath, "POST")
) {

    private var usernameParameter: String = JwtConstant.SPRING_SECURITY_FORM_USERNAME_KEY

    private var passwordParameter: String = JwtConstant.SPRING_SECURITY_FORM_PASSWORD_KEY

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val body = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)
        val username: String
        val password: String
        if (body.isBlank()) {
            username = request.getParameter(usernameParameter).orEmpty().trim()
            password = request.getParameter(passwordParameter).orEmpty()
        } else {
            val json = RequestBodyUtils.toJson(body)
            username = (json?.getStr(usernameParameter) ?: "").trim()
            password = json?.getStr(passwordParameter) ?: ""
        }
        if (username.isEmpty()) {
            throw BadCredentialsException("username must not be empty")
        }
        val authRequest = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(authRequest)
    }

    fun setUsernameParameter(usernameParameter: String) {
        this.usernameParameter = usernameParameter
    }

    fun setPasswordParameter(passwordParameter: String) {
        this.passwordParameter = passwordParameter
    }
}
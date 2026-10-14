package com.lhl.jwt.security.filters

import org.springframework.security.authentication.BadCredentialsException
import com.lhl.jwt.security.AntPatternRequestMatcher
import com.lhl.jwt.token.MobileCodeAuthenticationToken
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.RequestBodyUtils
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.util.StreamUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

/**
 * 手机号 + 验证码登录过滤器：支持 JSON 请求体或表单参数。
 */
class MobileCodeAuthenticationFilter(loginPath: String) : AbstractAuthenticationProcessingFilter(
    AntPatternRequestMatcher(loginPath, "POST")
) {

    private var mobileParameter: String = JwtConstant.SPRING_SECURITY_FORM_MOBILE_KEY

    private var codeParameter: String = JwtConstant.SPRING_SECURITY_FORM_CODE_KEY

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val body = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)
        val mobile: String
        val code: String
        if (body.isBlank()) {
            mobile = request.getParameter(mobileParameter).orEmpty().trim()
            code = request.getParameter(codeParameter).orEmpty().trim()
        } else {
            val json = RequestBodyUtils.toJson(body)
            mobile = (json?.getStr(mobileParameter) ?: "").trim()
            code = (json?.getStr(codeParameter) ?: "").trim()
        }
        if (mobile.isEmpty() || code.isEmpty()) {
            throw BadCredentialsException("mobile and code must not be empty")
        }
        val authRequest = MobileCodeAuthenticationToken(mobile, code)
        return authenticationManager.authenticate(authRequest)
    }

    fun setMobileParameter(mobileParameter: String) {
        this.mobileParameter = mobileParameter
    }

    fun setCodeParameter(codeParameter: String) {
        this.codeParameter = codeParameter
    }
}
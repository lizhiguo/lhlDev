package com.lhl.jwt.security.filters

import org.springframework.security.authentication.BadCredentialsException
import com.lhl.jwt.security.AntPatternRequestMatcher
import com.lhl.jwt.token.WxAuthenticationToken
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.RequestBodyUtils
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.util.StreamUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

/**
 * 微信登录过滤器（企业微信 / 公众号 / 小程序）：支持 JSON 请求体或表单参数。
 */
class WxAuthenticationFilter(loginPath: String) : AbstractAuthenticationProcessingFilter(
    AntPatternRequestMatcher(loginPath, "POST")
) {

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val body = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)
        val json = RequestBodyUtils.toJson(body)
        val wxId = (json?.getStr(JwtConstant.SPRING_SECURITY_FORM_WX_ID_KEY)
            ?: request.getParameter(JwtConstant.SPRING_SECURITY_FORM_WX_ID_KEY).orEmpty()).trim()
        val mobile = json?.getStr(SPRING_SECURITY_FORM_MOBILE_KEY)
            ?: request.getParameter(SPRING_SECURITY_FORM_MOBILE_KEY)
        val type = json?.getStr(JwtConstant.SPRING_SECURITY_FORM_TYPE_KEY)
            ?: request.getParameter(JwtConstant.SPRING_SECURITY_FORM_TYPE_KEY)
        if (wxId.isEmpty()) {
            throw BadCredentialsException("wxId must not be empty")
        }
        val authRequest = WxAuthenticationToken(wxId, mobile, type)
        return authenticationManager.authenticate(authRequest)
    }

    companion object {

        private const val SPRING_SECURITY_FORM_MOBILE_KEY = JwtConstant.SPRING_SECURITY_FORM_MOBILE_KEY
    }
}
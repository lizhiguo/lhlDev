package com.lhl.jwt.handler

import com.lhl.common.core.model.ApiResult
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

/**
 * 未认证入口点：请求缺少 token / token 失效时返回 401 + ApiResult。
 */
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val message = authException.message
        JwtResponseWriter.write(
            response,
            HttpStatus.UNAUTHORIZED.value(),
            ApiResult.fail(
                code = "401",
                msg = if (message.isNullOrBlank() || message in GENERIC_MESSAGES) "未认证，请先登录" else message
            )
        )
    }

    companion object {

        private val GENERIC_MESSAGES = setOf(
            "Full authentication is required to access this resource",
            "An Authentication object was not found in the SecurityContext"
        )
    }
}
package com.lhl.jwt.handler

import com.lhl.common.core.model.ApiResult
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

/**
 * 权限不足处理器：返回 403 + ApiResult，而不是默认的 HTML 错误页。
 */
class JwtAccessDeniedHandler : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        JwtResponseWriter.write(
            response,
            HttpStatus.FORBIDDEN.value(),
            ApiResult.fail(code = "403", msg = accessDeniedException.message ?: "权限不足")
        )
    }
}
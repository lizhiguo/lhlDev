package com.lhl.jwt.handler

import com.lhl.common.core.helper.toJsonString
import com.lhl.common.core.model.ApiResult
import jakarta.servlet.http.HttpServletResponse

/**
 * 统一的安全响应输出，保证鉴权相关错误也返回 ApiResult 结构，与业务接口保持一致。
 */
internal object JwtResponseWriter {

    fun write(response: HttpServletResponse, status: Int, result: ApiResult<Any>) {
        response.status = status
        response.contentType = "application/json;charset=UTF-8"
        response.characterEncoding = "UTF-8"
        response.writer.use { out ->
            out.print(result.toJsonString())
            out.flush()
        }
    }
}
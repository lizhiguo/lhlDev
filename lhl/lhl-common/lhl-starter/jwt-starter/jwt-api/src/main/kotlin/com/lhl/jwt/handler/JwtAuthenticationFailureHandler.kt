package com.lhl.jwt.handler

import com.lhl.common.core.helper.toJsonString
import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.exception.JwtVerificationFailedException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JwtAuthenticationFailureHandler : AuthenticationFailureHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.contentType = "Application/json"
        response.characterEncoding = "UTF-8"
        response.status = HttpStatus.UNAUTHORIZED.value()

        val result = when (exception) {
            is BadCredentialsException -> ApiResult.fail(msg = "用户名或密码错误")
            is JwtVerificationFailedException -> ApiResult.fail(msg = "token 验证失败")
            is JwtExpireException -> ApiResult.fail(msg = "登录过期，请重新登录")
            else -> ApiResult.fail(msg = exception.message)
        }

        response.writer.use {
            it.print(result.toJsonString())
            it.flush()
        }
        log.error { exception.message }
    }
}
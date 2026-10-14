package com.lhl.jwt.handler

import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.exception.JwtExpireException
import com.lhl.jwt.exception.JwtParseException
import com.lhl.jwt.exception.JwtSignatureVerifyException
import com.lhl.jwt.exception.JwtVerificationFailedException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * 认证失败处理器：统一降级为 401 + ApiResult。
 */
class JwtAuthenticationFailureHandler : AuthenticationFailureHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val msg = resolveMessage(exception)
        log.debug { "authentication failed: ${exception.message}" }
        JwtResponseWriter.write(response, HttpStatus.UNAUTHORIZED.value(), ApiResult.fail(code = "401", msg = msg))
    }

    private fun resolveMessage(exception: AuthenticationException): String = when (exception) {
        is JwtExpireException -> "登录已过期，请重新登录"
        is JwtSignatureVerifyException -> "token 签名无效"
        is JwtParseException -> "token 格式错误"
        is JwtVerificationFailedException -> exception.message ?: "token 验证失败"
        is UsernameNotFoundException, is DisabledException -> friendly(
            exception.message, "用户不存在或已被禁用"
        )
        is BadCredentialsException -> friendly(exception.message, "用户名或密码错误")
        else -> exception.message ?: "认证失败"
    }

    /**
     * Spring Security 内置的异常消息是英文且语义笼统，这里降级为中文提示；
     * 自定义消息（如"验证码错误或已过期"）原样透出。
     */
    private fun friendly(message: String?, default: String): String =
        if (message.isNullOrBlank() || message in GENERIC_MESSAGES) default else message

    companion object {

        private val GENERIC_MESSAGES = setOf(
            "Bad credentials",
            "User not found",
            "User is disabled",
            "Full authentication is required to access this resource"
        )
    }
}
package com.lhl.jwt.handler

import com.lhl.jwt.service.JwtTokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * 登录成功处理器：签发 access token 并写回响应头（前端保存后放入 Authorization 头）。
 *
 * 这里不写响应体：过滤器链会继续向下执行，写体会导致响应被提前提交。
 * 若需要在响应体中拿到 token，请基于 JwtTokenService#createTokenPair 自建端点。
 */
class LoginSuccessHandler(
    private val jwtTokenService: JwtTokenService
) : AuthenticationSuccessHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val user = authentication.principal as? UserDetails
        if (user == null) {
            log.warn { "login success but principal is not UserDetails: ${authentication.principal?.javaClass}" }
            return
        }
        val token = jwtTokenService.createAccessToken(user)
        TokenResponseWriter.writeToken(response, jwtTokenService.configuration, token)
        log.info { "login success, user = ${user.username}" }
    }
}
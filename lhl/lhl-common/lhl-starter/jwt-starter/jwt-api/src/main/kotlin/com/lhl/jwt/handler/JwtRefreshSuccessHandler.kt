package com.lhl.jwt.handler

import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.utils.JwtUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * 自动续期处理器。
 *
 * 当距上次签发超过 jwt.security.token-refresh-interval 时，
 * 重新签发 access token 并写回响应头（前端需读取响应头并替换本地 token）。
 *
 * 续期不影响本次请求：无论是否续期，认证都会继续沿过滤器链向下执行。
 */
class JwtRefreshSuccessHandler(
    private val jwtTokenService: JwtTokenService
) : AuthenticationSuccessHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val settings = jwtTokenService.configuration
        if (!settings.tokenAutoRefresh) {
            return
        }
        val token = JwtUtils.resolveToken(request, settings.header, settings.tokenPrefix, settings.cookieName)
        if (token.isNullOrBlank()) {
            return
        }
        try {
            val payload = jwtTokenService.parse(token)
            if (!jwtTokenService.shouldAutoRefresh(payload)) {
                return
            }
            val user = authentication.principal as? UserDetails ?: return
            val newToken = jwtTokenService.createAccessToken(user)
            TokenResponseWriter.writeToken(response, settings, newToken)
            log.info { "token auto refreshed, user = ${user.username}" }
        } catch (e: Exception) {
            log.warn { "auto refresh skipped: ${e.message}" }
        }
    }
}
package com.lhl.jwt.handler

import com.lhl.jwt.config.JwtSettings
import jakarta.servlet.http.HttpServletResponse

/**
 * token 写回响应：请求头 + 可选的 HttpOnly Cookie。
 *
 * Cookie 模式下手工拼 `Set-Cookie`，因为 Servlet API 不支持 SameSite 属性。
 */
internal object TokenResponseWriter {

    fun writeToken(response: HttpServletResponse, settings: JwtSettings, token: String) {
        response.setHeader(settings.header, settings.tokenPrefix + token)
        response.setHeader(com.lhl.jwt.utils.JwtConstant.ACCESS_CONTROL_EXPOSE_HEADER, settings.header)
        writeCookie(response, settings, token)
    }

    fun writeCookie(response: HttpServletResponse, settings: JwtSettings, token: String) {
        val cookie = settings.cookie
        val name = settings.cookieName ?: return
        val builder = StringBuilder(name).append('=').append(token)
            .append("; Path=").append(cookie.path)
        if (cookie.httpOnly) {
            builder.append("; HttpOnly")
        }
        if (cookie.secure) {
            builder.append("; Secure")
        }
        cookie.domain?.let { builder.append("; Domain=").append(it) }
        cookie.sameSite?.let { builder.append("; SameSite=").append(it) }
        cookie.maxAge?.let { builder.append("; Max-Age=").append(it.seconds) }
        response.addHeader("Set-Cookie", builder.toString())
    }
}
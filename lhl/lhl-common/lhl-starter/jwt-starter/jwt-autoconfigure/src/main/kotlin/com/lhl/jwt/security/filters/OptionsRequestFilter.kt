package com.lhl.jwt.security.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 放行 OPTIONS 预检请求，并回显允许的方法与请求头。
 *
 * 只处理预检请求，其余请求继续沿过滤器链执行。
 */
class OptionsRequestFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!request.method.equals("OPTIONS", ignoreCase = true)) {
            filterChain.doFilter(request, response)
            return
        }
        response.setHeader(ALLOW_METHODS_HEADER, ALLOWED_METHODS)
        request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS)?.let {
            response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, it)
        }
        response.status = HttpServletResponse.SC_OK
    }

    companion object {

        private const val ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH"
        private const val ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods"
        private const val ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers"
        private const val ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"
    }
}
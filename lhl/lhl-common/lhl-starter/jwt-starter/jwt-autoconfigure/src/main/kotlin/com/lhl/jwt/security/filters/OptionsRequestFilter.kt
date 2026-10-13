package com.lhl.jwt.security.filters

import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class OptionsRequestFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.method.equals("OPTIONS", ignoreCase = true)) {
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD")
            response.setHeader("Access-Control-Allow-Headers", response.getHeader("Access-Control-Request-Headers"))
            return
        }
        filterChain.doFilter(request, response)
    }
}
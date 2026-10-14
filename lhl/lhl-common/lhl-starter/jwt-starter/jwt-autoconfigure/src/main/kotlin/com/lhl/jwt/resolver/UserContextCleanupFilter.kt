package com.lhl.jwt.resolver

import com.lhl.common.core.helper.UserContext
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * 请求结束后清理 [UserContext]（ThreadLocal），避免线程复用导致的数据串号。
 */
class UserContextCleanupFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            UserContext.clear()
        }
    }

    companion object {

        @JvmField
        val ORDER: Int = Ordered.HIGHEST_PRECEDENCE + 10
    }
}
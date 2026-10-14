package com.lhl.jwt.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.AntPathMatcher

/**
 * Ant 风格的请求匹配器。
 *
 * Spring Security 7 移除了 AntPathRequestMatcher，而 PathPattern 语法不再支持中间的 `**`
 * （例如 `/**/login`）。为了保持既有配置语义，这里基于 spring-core 的 AntPathMatcher 自实现，
 * 同时过滤掉 contextPath。
 */
class AntPatternRequestMatcher(
    private val pattern: String,
    private val httpMethod: String? = null
) : RequestMatcher {

    private val pathMatcher = AntPathMatcher()

    override fun matches(request: HttpServletRequest): Boolean {
        if (httpMethod != null && !httpMethod.equals(request.method, ignoreCase = true)) {
            return false
        }
        return matchPath(request)
    }

    private fun matchPath(request: HttpServletRequest): Boolean {
        val path = request.requestURI.removePrefix(request.contextPath)
        return pathMatcher.match(pattern, path)
    }

    override fun toString(): String =
        if (httpMethod == null) pattern else "$httpMethod $pattern"
}
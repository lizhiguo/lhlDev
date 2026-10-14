package com.lhl.jwt.resolver

import com.lhl.common.core.annotation.CurrentUser
import com.lhl.common.core.helper.UserContext
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.utils.JwtUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 支持在 Controller 参数上使用 `@CurrentUser`。
 *
 * 解析顺序：
 * 1. 已认证时从 SecurityContext 取，并写入 [UserContext] 供业务代码读取；
 * 2. 未认证时回退为解析请求中的 token；
 * 3. 都没有时返回 null。
 *
 * 可注入类型：String（用户名）、UserDetails、JwtUser。
 */
class LoginUserHandlerMethodArgumentResolver(
    private val jwtTokenService: JwtTokenService,
    private val jwtUserDetailsService: JwtUserDetailsService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        if (!parameter.hasParameterAnnotation(CurrentUser::class.java)) {
            return false
        }
        val type = parameter.parameterType
        return type.isAssignableFrom(String::class.java) || UserDetails::class.java.isAssignableFrom(type)
    }

    override fun resolveArgument(
        methodParameter: MethodParameter,
        modelAndViewContainer: ModelAndViewContainer?,
        request: NativeWebRequest,
        webDataBinderFactory: WebDataBinderFactory?
    ): Any? {
        val userDetails = resolveUserDetails(request) ?: return null
        UserContext.set(userDetails.username)
        val type = methodParameter.parameterType
        return when {
            UserDetails::class.java.isAssignableFrom(type) -> userDetails
            else -> userDetails.username
        }
    }

    private fun resolveUserDetails(request: NativeWebRequest): UserDetails? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        val fromContext = authentication?.principal as? UserDetails
        if (fromContext != null && authentication.isAuthenticated) {
            return fromContext
        }
        val settings = jwtTokenService.configuration
        val servletRequest = request.getNativeRequest(HttpServletRequest::class.java) ?: return null
        val token = JwtUtils.resolveToken(servletRequest, settings.header, settings.tokenPrefix, settings.cookieName)
            ?: return null
        val username = jwtTokenService.extractUsername(token) ?: return null
        return jwtUserDetailsService.loadUserByUsername(username)
    }
}
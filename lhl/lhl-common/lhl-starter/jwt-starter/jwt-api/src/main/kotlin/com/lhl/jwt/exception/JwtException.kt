package com.lhl.jwt.exception

import org.springframework.security.core.AuthenticationException

/**
 * JWT 异常基类。
 *
 * 所有 JWT 相关的失败（解析、验签、过期、黑名单）都继承该类，
 * 便于在 JwtAuthenticationFailureHandler 中统一按类型降级为业务响应。
 *
 * 注意：继承 AuthenticationException 而不是 BizException —— BizException 的构造器是私有的，无法被外部继承。
 */
open class JwtException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null
) : AuthenticationException(message) {

    init {
        if (cause != null) {
            initCause(cause)
        }
    }
}
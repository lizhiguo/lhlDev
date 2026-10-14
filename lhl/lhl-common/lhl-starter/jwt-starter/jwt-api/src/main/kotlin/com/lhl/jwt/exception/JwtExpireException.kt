package com.lhl.jwt.exception

import com.lhl.jwt.utils.JwtConstant

/**
 * token 已过期（同时用于命中黑名单后的强制失效）。
 */
class JwtExpireException @JvmOverloads constructor(
    message: String = JwtConstant.TOKEN_EXPIRE,
    cause: Throwable? = null
) : JwtException(message, cause)
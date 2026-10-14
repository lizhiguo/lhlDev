package com.lhl.jwt.exception

/**
 * 请求参数非法（登录参数缺失等），属于 400 语义，不做 401 降级处理。
 */
class JwtParamException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)
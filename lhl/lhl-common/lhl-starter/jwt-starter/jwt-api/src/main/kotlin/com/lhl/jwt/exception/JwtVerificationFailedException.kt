package com.lhl.jwt.exception

import com.lhl.jwt.utils.JwtConstant

/**
 * 验签通过但整体校验失败：iss / aud 不匹配、nbf 未生效、token 类型不符等。
 */
class JwtVerificationFailedException @JvmOverloads constructor(
    message: String = JwtConstant.TOKEN_VERIFY_ERROR,
    cause: Throwable? = null
) : JwtException(message, cause)
package com.lhl.jwt.exception

import com.lhl.jwt.utils.JwtConstant

/**
 * 签名验证失败：签名不匹配，或使用了错误的密钥 / 被篡改。
 */
class JwtSignatureVerifyException @JvmOverloads constructor(
    message: String = JwtConstant.TOKEN_VERIFY_ERROR,
    cause: Throwable? = null
) : JwtException(message, cause)
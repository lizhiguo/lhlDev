package com.lhl.jwt.exception

import com.lhl.jwt.utils.JwtConstant

/**
 * token 结构非法，无法被解析为 JWT。
 */
class JwtParseException @JvmOverloads constructor(
    message: String = JwtConstant.TOKEN_PARSE_ERROR,
    cause: Throwable? = null
) : JwtException(message, cause)
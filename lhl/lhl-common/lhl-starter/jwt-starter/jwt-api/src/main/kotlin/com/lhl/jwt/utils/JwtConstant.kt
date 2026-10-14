package com.lhl.jwt.utils

/**
 * JWT 相关常量。
 */
object JwtConstant {

    /** 认证信息响应头 */
    const val AUTHORIZATION_HEADER: String = "Authorization"

    /** CORS 允许浏览器读取的响应头 */
    const val ACCESS_CONTROL_EXPOSE_HEADER: String = "Access-Control-Expose-Headers"

    /** 认证信息前缀 */
    const val AUTHORIZATION_START_STRING: String = "Bearer "

    const val TOKEN_NOT_EMPTY: String = "token must not be empty"
    const val TOKEN_EXPIRE: String = "token is expire"
    const val TOKEN_FORMAT_ERROR: String = "token format error"
    const val TOKEN_VERIFY_ERROR: String = "token verify error"
    const val TOKEN_PARSE_ERROR: String = "token parse error"
    const val TOKEN_TYPE_MISMATCH: String = "token type mismatch"

    const val SPRING_SECURITY_FORM_USERNAME_KEY: String = "username"
    const val SPRING_SECURITY_FORM_PASSWORD_KEY: String = "password"
    const val SPRING_SECURITY_FORM_MOBILE_KEY: String = "mobile"
    const val SPRING_SECURITY_FORM_CODE_KEY: String = "code"
    const val SPRING_SECURITY_FORM_WX_ID_KEY: String = "wxId"
    const val SPRING_SECURITY_FORM_TYPE_KEY: String = "type"

    /** access token 类型标识（claim: typ） */
    const val TOKEN_TYPE_ACCESS: String = "access"

    /** refresh token 类型标识（claim: typ） */
    const val TOKEN_TYPE_REFRESH: String = "refresh"

    /** 用户名 claim */
    const val CLAIM_USERNAME: String = "username"

    /** 权限 claim */
    const val CLAIM_AUTHORITIES: String = "authorities"

    /** token 类型 claim */
    const val CLAIM_TOKEN_TYPE: String = "tokenType"

    /** 黑名单缓存 key 前缀 */
    const val BLACKLIST_KEY_PREFIX: String = "jwt:blacklist:"

    /** refresh token 元数据 key 前缀 */
    const val REFRESH_TOKEN_KEY_PREFIX: String = "jwt:refresh:"
}
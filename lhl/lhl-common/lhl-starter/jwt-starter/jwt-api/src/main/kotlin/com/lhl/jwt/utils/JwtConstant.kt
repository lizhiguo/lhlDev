package com.lhl.jwt.utils

object JwtConstant {
    const val AUTHORIZATION_HEADER = "Authorization"
    const val ACCESS_CONTROL_EXPOSE_HEADER = "Access-Control-Expose-Headers"
    const val AUTHORIZATION_START_STRING = "Bearer "
    const val TOKEN_NOT_EMPTY = "token must not be empty"
    const val TOKEN_EXPIRE = "token is expire"
    const val TOKEN_FORMAT_ERROR = "token format error"
    const val TOKEN_VERIFY_ERROR = "token verify error"
    const val TOKEN_PARSE_ERROR = "token parse error"
    const val SPRING_SECURITY_FORM_USERNAME_KEY = "username"
    const val SPRING_SECURITY_FORM_PASSWORD_KEY = "password"
}
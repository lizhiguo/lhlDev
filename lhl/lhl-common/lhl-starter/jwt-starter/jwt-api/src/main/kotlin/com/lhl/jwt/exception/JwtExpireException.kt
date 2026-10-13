package com.lhl.jwt.exception

import org.springframework.security.core.AuthenticationException

class JwtExpireException : AuthenticationException {

    constructor(msg: String) : super(msg)
    constructor(msg: String, t: Throwable) : super(msg, t)
}
package com.lhl.jwt.exception

import org.springframework.security.core.AuthenticationException

class JwtSignatureVerifyException(message: String) : AuthenticationException(message)
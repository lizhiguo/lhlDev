package com.lhl.jwt.exception

import org.springframework.security.core.AuthenticationException

class JwtVerificationFailedException(msg: String) : AuthenticationException(msg)
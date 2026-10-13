package com.lhl.jwt.exception

import org.springframework.security.core.AuthenticationException

class JwtParseException(message: String) : AuthenticationException(message)
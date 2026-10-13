package com.lhl.jwt.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt.security")
data class JwtProperties(
    var permitUrls: String? = null,
    var tokenRefreshInterval: Int = 0,
    var clearInterval: Int = 60000
)
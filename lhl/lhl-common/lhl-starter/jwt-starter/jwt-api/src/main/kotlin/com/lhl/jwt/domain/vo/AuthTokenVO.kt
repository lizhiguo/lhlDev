package com.lhl.jwt.domain.vo

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

@Schema(name = "令牌对")
data class AuthTokenVO(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Long
) : Serializable {

    companion object {

        private const val serialVersionUID = 1L
    }
}
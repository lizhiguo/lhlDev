package com.lhl.jwt.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

@Schema(name = "JWT数据载体")
data class PayloadDTO(
    var jti: String? = null,
    var iss: String? = null,
    var exp: Long? = null,
    var sub: String? = null,
    var aud: String? = null,
    var nbf: Long? = null,
    var iat: Long? = null,
    var username: String? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var isAdmin: Boolean? = null,
    var authorities: List<String>? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 5988619597830511341L
    }
}
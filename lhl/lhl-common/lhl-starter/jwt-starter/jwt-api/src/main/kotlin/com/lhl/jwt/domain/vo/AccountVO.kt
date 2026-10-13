package com.lhl.jwt.domain.vo

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.util.Date

@Schema(name = "AccountVO")
data class AccountVO(
    val openid: String? = null,
    val sessionKey: String? = null,
    val nickName: String? = null,
    val gender: Int? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val deleted: Int? = null,
    val createTime: Date? = null,
    val lastUpdateTime: Date? = null
) : Serializable
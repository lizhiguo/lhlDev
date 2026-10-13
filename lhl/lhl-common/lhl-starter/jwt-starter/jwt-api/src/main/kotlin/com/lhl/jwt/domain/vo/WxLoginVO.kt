package com.lhl.jwt.domain.vo

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "WxLoginVO")
data class WxLoginVO(
    val openid: String? = null,
    val session_key: String? = null,
    val errcode: String? = null,
    val errmsg: String? = null
)
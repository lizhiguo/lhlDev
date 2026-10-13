package com.lhl.jwt.domain.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "微信数据")
data class WxExtBO(
    var openid: String? = null,
    var sessionKey: String? = null,
    var nickName: String? = null,
    var gender: Int? = null,
    var avatarUrl: String? = null,
    var country: String? = null,
    var province: String? = null,
    var city: String? = null,
    var deleted: Int? = null
)
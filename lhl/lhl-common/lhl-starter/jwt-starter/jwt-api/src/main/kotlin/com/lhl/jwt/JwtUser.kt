package com.lhl.jwt

import com.lhl.jwt.domain.bo.WxExtBO
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

@Schema(name = "JwtUser")
class JwtUser(
    username: String,
    password: String,
    authorities: Collection<GrantedAuthority>
) : User(username, password, authorities) {

    var salt: String? = null
    var userId: String? = null
    var wxExtBO: WxExtBO? = null
}
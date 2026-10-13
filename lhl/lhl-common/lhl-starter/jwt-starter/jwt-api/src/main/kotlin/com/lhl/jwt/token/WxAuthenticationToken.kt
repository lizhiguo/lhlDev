package com.lhl.jwt.token

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

@Schema(name = "微信通用token")
class WxAuthenticationToken(
    var wxId: String,
    var mobile: String?,
    var type: String?
) : AbstractAuthenticationToken(null) {

    constructor(
        wxId: String,
        authorities: Collection<GrantedAuthority>
    ) : this(wxId, null, null) {
        isAuthenticated = true
    }

    constructor(
        wxId: String,
        mobile: String?,
        type: String?,
        authorities: Collection<GrantedAuthority>
    ) : this(wxId, mobile, type) {
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = mobile
    override fun getPrincipal(): Any = wxId

    override fun setAuthenticated(isAuthenticated: Boolean) {
        if (isAuthenticated) {
            throw IllegalArgumentException(
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead"
            )
        }
        super.setAuthenticated(false)
    }

    override fun eraseCredentials() {
        super.eraseCredentials()
    }
}
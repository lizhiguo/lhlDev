package com.lhl.jwt.token

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Collections

class JwtAuthenticationToken : AbstractAuthenticationToken {

    private val _principal: UserDetails?
    private val _credentials: String?
    val token: String?

    constructor(token: String) : super(Collections.emptyList()) {
        this._principal = null
        this._credentials = null
        this.token = token
    }

    constructor(
        principal: UserDetails,
        token: String?,
        authorities: Collection<GrantedAuthority>
    ) : super(authorities) {
        this._principal = principal
        this._credentials = null
        this.token = token
        isAuthenticated = true
    }

    override fun getCredentials(): String? = _credentials
    override fun getPrincipal(): Any? = _principal
}
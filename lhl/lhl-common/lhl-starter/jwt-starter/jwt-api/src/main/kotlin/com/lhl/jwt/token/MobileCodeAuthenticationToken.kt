package com.lhl.jwt.token

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.SpringSecurityCoreVersion

class MobileCodeAuthenticationToken(
    private val _principal: Any,
    private var _credentials: String?
) : AbstractAuthenticationToken(null) {

    constructor(
        principal: Any,
        authorities: Collection<GrantedAuthority>
    ) : this(principal, null) {
        super.setAuthenticated(true)
    }

    override fun getCredentials(): String? = _credentials
    override fun getPrincipal(): Any = _principal

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
        _credentials = null
    }

    companion object {
        private const val serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID
    }
}
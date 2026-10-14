package com.lhl.jwt.token

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.Collections

/**
 * 手机号 + 验证码认证令牌。
 *
 * @param authorities Provider 认证成功后传入的权限；为空表示尚未认证
 */
class MobileCodeAuthenticationToken(
    private val _principal: Any,
    private var _credentials: String?,
    authorities: Collection<GrantedAuthority> = Collections.emptyList()
) : AbstractAuthenticationToken(authorities) {

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
}
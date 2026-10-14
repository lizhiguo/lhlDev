package com.lhl.jwt.token

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Collections

/**
 * JWT 认证令牌。
 *
 * - 构造入参只有 token：未认证状态，由 JwtAuthenticationFilter 从请求中提取后创建；
 * - 构造入参含 principal：已认证状态，由 JwtAuthenticationProvider 认证成功后返回。
 */
class JwtAuthenticationToken : AbstractAuthenticationToken {

    private val _principal: UserDetails?
    private val _credentials: String?

    /** 原始 JWT 字符串 */
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
        super.setAuthenticated(true)
    }

    override fun getCredentials(): String? = _credentials
    override fun getPrincipal(): Any? = _principal
}
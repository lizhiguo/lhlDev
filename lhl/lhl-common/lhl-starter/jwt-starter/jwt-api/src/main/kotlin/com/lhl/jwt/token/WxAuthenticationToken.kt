package com.lhl.jwt.token

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.Collections

/**
 * 微信登录认证令牌（企业微信 / 公众号 / 小程序）。
 *
 * @param wxId   企业微信 userId、公众号/小程序 openId
 * @param mobile 手机号，可空
 * @param type   cp=企业微信，mp=公众号，miniApp=小程序
 */
@Schema(name = "微信通用token")
class WxAuthenticationToken(
    var wxId: String,
    var mobile: String? = null,
    var type: String? = null,
    authorities: Collection<GrantedAuthority> = Collections.emptyList(),
    authenticated: Boolean = false
) : AbstractAuthenticationToken(authorities) {

    init {
        if (authenticated) {
            super.setAuthenticated(true)
        }
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
}
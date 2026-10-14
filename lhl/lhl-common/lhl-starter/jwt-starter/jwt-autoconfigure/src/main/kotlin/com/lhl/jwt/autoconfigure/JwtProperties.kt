package com.lhl.jwt.autoconfigure

import com.lhl.jwt.config.JwtSettings
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * JWT Starter 配置项，前缀 `jwt.security`。
 *
 * 完整示例见模块根目录 JWT-STARTER.md；最小配置只需要 `secret`（长度 >= 32）。
 */
@ConfigurationProperties(prefix = "jwt.security")
data class JwtProperties(
    /** 总开关，false 时不装配任何安全组件 */
    val enabled: Boolean = true,
    /** 全局签名密钥，长度必须 >= 32 */
    val secret: String = "",
    /** 签发者，校验时会校验 iss */
    val issuer: String? = null,
    /** 受众，校验时会校验 aud */
    val audience: String? = null,
    /** access token 有效期 */
    val accessTokenExpiration: Duration = Duration.ofHours(2),
    /** refresh token 有效期 */
    val refreshTokenExpiration: Duration = Duration.ofDays(7),
    /** 自动续期间隔，超过该时长才回写新 token；0 表示关闭 */
    val tokenRefreshInterval: Duration = Duration.ZERO,
    /** token 承载的请求头 */
    val header: String = "Authorization",
    /** token 前缀 */
    val tokenPrefix: String = "Bearer ",
    /** 无需认证的路径（Ant 风格） */
    val permitUrls: List<String> = emptyList(),
    /** 完全绕过 security 过滤器链的路径（静态资源、favicon 等），走的是 WebSecurity#ignoring */
    val ignoreUrls: List<String> = emptyList(),
    /**
     * 本 starter 过滤器链的作用域（Ant 风格），为空表示接管全部路径。
     *
     * 场景：项目里同时存在「无状态 API」和「有状态页面（SSO / Passkey）」时，
     * 把本 starter 限定到 API 前缀下，业务方再自行注册另一条 SecurityFilterChain，
     * 避免两条链的 Session 策略互相冲突。
     */
    val securityMatchers: List<String> = emptyList(),
    /** 关闭内置的刷新端点 */
    val endpointEnabled: Boolean = true,
    /** 黑名单 / refresh 元数据的兜底清理间隔 */
    val clearInterval: Duration = Duration.ofMinutes(1),
    val paths: Paths = Paths(),
    val cookie: Cookie = Cookie(),
    val cors: Cors = Cors()
) {

    /** 认证相关端点路径 */
    data class Paths(
        val login: String = "/**/login",
        val mobileLogin: String = "/mobileCodeLogin",
        val wxLogin: String = "/wxLogin",
        val logout: String = "/logout",
        val refresh: String = "/refresh"
    )

    /** HttpOnly Cookie 承载 token */
    data class Cookie(
        val enabled: Boolean = false,
        val name: String = "jwt-token",
        val httpOnly: Boolean = true,
        val secure: Boolean = false,
        val sameSite: String? = "Lax",
        val domain: String? = null,
        val path: String = "/",
        val maxAge: Duration? = null
    )

    /** 跨域配置；allowedOriginsPatterns 为空时不做跨域限制 */
    data class Cors(
        val enabled: Boolean = false,
        val allowedOriginPatterns: List<String> = listOf("*"),
        val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"),
        val allowedHeaders: List<String> = listOf("*"),
        val exposedHeaders: List<String> = listOf("Authorization"),
        val allowCredentials: Boolean = true,
        val maxAge: Duration = Duration.ofHours(1)
    )

    /** 转换为与 Spring 无关的运行期配置 */
    fun toSettings(): JwtSettings = JwtSettings(
        secret = secret,
        issuer = issuer,
        audience = audience,
        accessTokenExpiration = accessTokenExpiration,
        refreshTokenExpiration = refreshTokenExpiration,
        tokenRefreshInterval = tokenRefreshInterval,
        header = header,
        tokenPrefix = tokenPrefix,
        loginPath = paths.login,
        mobileLoginPath = paths.mobileLogin,
        wxLoginPath = paths.wxLogin,
        logoutPath = paths.logout,
        refreshPath = paths.refresh,
        permitUrls = permitUrls,
        blacklistEnabled = true,
        clearInterval = clearInterval,
        cookie = JwtSettings.Cookie(
            enabled = cookie.enabled,
            name = cookie.name,
            httpOnly = cookie.httpOnly,
            secure = cookie.secure,
            sameSite = cookie.sameSite,
            domain = cookie.domain,
            path = cookie.path,
            maxAge = cookie.maxAge ?: accessTokenExpiration
        )
    )

    /**
     * 需要放行的路径：显式白名单 + 内置端点。
     *
     * @return 去重后的 Ant 风格路径
     */
    fun resolvePermitUrls(): List<String> {
        val defaults = mutableListOf(paths.login, paths.mobileLogin, paths.wxLogin, paths.logout)
        if (endpointEnabled) {
            defaults += paths.refresh
        }
        defaults += "/error"
        return (permitUrls + defaults).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }
}
package com.lhl.jwt.config

import java.time.Duration

/**
 * JWT 运行期配置快照。
 *
 * 放在 jwt-api 而不是 autoconfigure 层，是为了让核心逻辑（[com.lhl.jwt.service.JwtTokenService]）
 * 不依赖 Spring Boot 的配置绑定机制，从而可以脱离容器单测。
 *
 * 由 `jwt-autoconfigure` 中的 `JwtProperties#toSettings()` 完成映射。
 */
data class JwtSettings(
    /** 全局签名密钥，生产环境必须通过环境变量注入，长度 >= 32 */
    val secret: String,
    /** 签发者，校验时会校验 iss */
    val issuer: String? = null,
    /** 受众，校验时会校验 aud */
    val audience: String? = null,
    /** access token 有效期 */
    val accessTokenExpiration: Duration = Duration.ofHours(2),
    /** refresh token 有效期 */
    val refreshTokenExpiration: Duration = Duration.ofDays(7),
    /** 自动续期阈值：距上次签发超过该间隔时在响应头回写新 token，Duration.ZERO 表示关闭 */
    val tokenRefreshInterval: Duration = Duration.ZERO,
    /** token 承载的请求头 */
    val header: String = "Authorization",
    /** token 前缀 */
    val tokenPrefix: String = "Bearer ",
    /** 登录 / 手机号登录 / 微信登录 / 登出 / 刷新 的端点路径 */
    val loginPath: String = "/**/login",
    val mobileLoginPath: String = "/mobileCodeLogin",
    val wxLoginPath: String = "/wxLogin",
    val logoutPath: String = "/logout",
    val refreshPath: String = "/refresh",
    /** 无需认证即可访问的路径（Ant 风格） */
    val permitUrls: List<String> = emptyList(),
    /** 是否启用登出黑名单 */
    val blacklistEnabled: Boolean = true,
    /** 黑名单/refresh 元数据的兜底清理间隔 */
    val clearInterval: Duration = Duration.ofMinutes(1),
    /** HttpOnly Cookie 承载 token 的配置 */
    val cookie: Cookie = Cookie()
) {

    /** 是否开启 token 自动续期 */
    val tokenAutoRefresh: Boolean
        get() = !tokenRefreshInterval.isZero && !tokenRefreshInterval.isNegative

    /** Cookie 模式下需要读取的 Cookie 名称，未启用时为 null */
    val cookieName: String?
        get() = cookie.name.takeIf { cookie.enabled && it.isNotBlank() }

    data class Cookie(
        val enabled: Boolean = false,
        val name: String = "jwt-token",
        val httpOnly: Boolean = true,
        val secure: Boolean = false,
        val sameSite: String? = "Lax",
        val domain: String? = null,
        val path: String = "/",
        /** 与 access token 有效期保持一致；null 表示不设置 Max-Age */
        val maxAge: Duration? = null
    )
}
package com.lhl.jwt.utils

import jakarta.servlet.http.HttpServletRequest
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * 无状态的 JWT 工具方法：密钥派生、token 提取、剩余有效期计算。
 *
 * 真正的签发 / 校验逻辑放在 [com.lhl.jwt.service.JwtTokenService]。
 */
object JwtUtils {

    private const val HMAC_ALGORITHM = "HmacSHA256"

    private const val DIGEST_ALGORITHM = "SHA-256"

    /**
     * 派生 HMAC-SHA256 签名密钥。
     *
     * 密钥 = SHA-256(全局密钥 + ":" + 用户标识)，因此：
     * 1. 不同用户拥有不同的签名密钥，一个用户的 token 无法冒充另一个用户；
     * 2. 密钥来源于配置中的全局密钥（>= 32 字符），攻击者无法自行推导。
     */
    @JvmStatic
    fun deriveKey(secret: String, salt: String?): SecretKey {
        val material = (secret + ":" + salt.orEmpty()).toByteArray(StandardCharsets.UTF_8)
        val digest = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(material)
        return SecretKeySpec(digest, HMAC_ALGORITHM)
    }

    /**
     * 依次从 `Authorization: Bearer xxx` 响应头、Cookie 中提取 token。
     *
     * @return token 字符串；不存在时返回 null
     */
    @JvmStatic
    @JvmOverloads
    fun resolveToken(
        request: HttpServletRequest,
        header: String = JwtConstant.AUTHORIZATION_HEADER,
        prefix: String = JwtConstant.AUTHORIZATION_START_STRING,
        cookieName: String? = null
    ): String? {
        val fromHeader = request.getHeader(header)
        if (!fromHeader.isNullOrBlank()) {
            return fromHeader.removePrefix(prefix).trim().ifBlank { null }
        }
        if (cookieName != null) {
            val cookie = request.cookies?.firstOrNull { it.name == cookieName }
            if (cookie != null && !cookie.value.isNullOrBlank()) {
                return cookie.value.trim()
            }
        }
        return null
    }

    /** 计算距离过期的剩余毫秒数，用于黑名单 TTL */
    @JvmStatic
    fun remainingMillis(expiresAt: Long): Long = expiresAt - System.currentTimeMillis()
}
package com.lhl.jwt.store

/**
 * refresh token 元数据。
 *
 * @param jti refresh token 的唯一标识
 * @param username 所属用户
 * @param expiresAt 过期时间戳（epoch millis）
 */
data class RefreshToken(
    val jti: String,
    val username: String,
    val expiresAt: Long
)

/**
 * refresh token 存储，用于 refresh token 轮换（rotation）与主动吊销。
 *
 * 默认实现 [InMemoryRefreshTokenStore] 仅适用于单机；
 * 集群场景请实现该接口并注册为 Bean 替换为 Redis / DB 实现。
 */
interface RefreshTokenStore {

    /** 保存（覆盖）refresh token 元数据 */
    fun save(refreshToken: RefreshToken)

    /** 按 jti 查询 */
    fun find(jti: String): RefreshToken?

    /** 按 jti 吊销 */
    fun revoke(jti: String)

    /** 吊销某个用户的全部 refresh token（登出时调用） */
    fun revokeByUsername(username: String)

    /** 清理已过期条目 */
    fun removeExpired()
}
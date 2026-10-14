package com.lhl.jwt.store

import com.lhl.jwt.utils.JwtConstant
import java.time.Duration

/**
 * token 黑名单。登出时将 token 加入黑名单，实现"立即失效"（不等待自然过期）。
 *
 * 默认实现 [InMemoryTokenBlacklistStore] 仅适用于单机；
 * 集群场景请实现该接口并注册为 Bean 替换为 Redis / DB 实现。
 */
interface TokenBlacklistStore {

    /** token 是否已被吊销 */
    fun isBlacklisted(token: String): Boolean

    /**
     * 吊销 token。
     *
     * @param ttl 剩余有效期；<= 0 时视为无需吊销（token 本身已过期）
     */
    fun revoke(token: String, ttl: Duration)

    /** 清理已过期条目 */
    fun removeExpired()

    /** 当前吊销的 token 数量 */
    fun size(): Int

    companion object {
        /** 黑名单 key 生成规则，便于自定义实现复用 */
        @JvmStatic
        fun keyOf(token: String): String = JwtConstant.BLACKLIST_KEY_PREFIX + token
    }
}
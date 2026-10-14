package com.lhl.jwt.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 * 轻量级内存缓存，基于 ConcurrentHashMap。
 *
 * jwt-starter 内部已用语义化的 [com.lhl.jwt.store.TokenBlacklistStore] /
 * [com.lhl.jwt.store.RefreshTokenStore] 抽象替代直接使用本缓存，
 * 保留该工具类仅为兼容既有代码与通用缓存场景。
 */
object CacheManager {

    private val log = KotlinLogging.logger {}

    private val cacheData = ConcurrentHashMap<String, CacheData<Any>>()

    /** 永不过期 */
    const val ALWAYS_ACTIVE: Long = 0

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> getData(key: String): T? {
        val data = cacheData[key] ?: return null
        if (data.isExpired()) {
            cacheData.remove(key)
            return null
        }
        return data.data as T
    }

    @JvmStatic
    @JvmOverloads
    fun <T : Any> setData(key: String, data: T, expire: Long = ALWAYS_ACTIVE) {
        cacheData[key] = CacheData<Any>(data, expire)
    }

    @JvmStatic
    fun clear(key: String) {
        cacheData.remove(key)
    }

    @JvmStatic
    fun clearAll() {
        cacheData.clear()
    }

    /** 清理已过期条目，返回被清理的条数 */
    @JvmStatic
    fun clearExpire(): Int {
        var cleared = 0
        val iterator = cacheData.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.isExpired()) {
                iterator.remove()
                cleared++
            }
        }
        if (cleared > 0) {
            log.debug { "clear expired jwt cache, cleared = $cleared, remaining = ${cacheData.size}" }
        }
        return cleared
    }

    @JvmStatic
    fun size(): Int = cacheData.size

    private class CacheData<T>(val data: T, expire: Long) {

        /** 0 表示永不过期 */
        private val expireAt: Long =
            if (expire <= ALWAYS_ACTIVE) ALWAYS_ACTIVE else System.currentTimeMillis() + expire

        fun isExpired(): Boolean = expireAt > ALWAYS_ACTIVE && System.currentTimeMillis() >= expireAt
    }
}
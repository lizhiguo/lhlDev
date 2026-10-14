package com.lhl.jwt.store

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 基于内存的 TokenBlacklistStore 默认实现。
 *
 * 写入时按 clearInterval 顺带做一次过期清理（机会式清理），
 * 无需引入调度线程，黑名单也不会无限膨胀。
 */
class InMemoryTokenBlacklistStore(
    private val clearInterval: Duration = Duration.ofMinutes(1)
) : TokenBlacklistStore {

    private val log = KotlinLogging.logger {}

    private val entries = ConcurrentHashMap<String, Long>()

    private val lastCleanAt = AtomicLong(System.currentTimeMillis())

    override fun isBlacklisted(token: String): Boolean {
        if (token.isEmpty()) {
            return false
        }
        val key = TokenBlacklistStore.keyOf(token)
        val expireAt = entries[key] ?: return false
        if (expireAt <= System.currentTimeMillis()) {
            entries.remove(key)
            return false
        }
        return true
    }

    override fun revoke(token: String, ttl: Duration) {
        if (token.isEmpty() || ttl.isZero || ttl.isNegative) {
            return
        }
        cleanIfNecessary()
        entries[TokenBlacklistStore.keyOf(token)] = System.currentTimeMillis() + ttl.toMillis()
        log.debug { "revoke token, expire in ${ttl.toMillis()}ms" }
    }

    override fun removeExpired() {
        val now = System.currentTimeMillis()
        var cleared = 0
        val iterator = entries.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value <= now) {
                iterator.remove()
                cleared++
            }
        }
        lastCleanAt.set(now)
        if (cleared > 0) {
            log.debug { "clean expired blacklist, cleared = $cleared, remaining = ${entries.size}" }
        }
    }

    override fun size(): Int = entries.size

    private fun cleanIfNecessary() {
        val now = System.currentTimeMillis()
        if (now - lastCleanAt.get() >= clearInterval.toMillis()) {
            removeExpired()
        }
    }
}
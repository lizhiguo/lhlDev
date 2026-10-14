package com.lhl.jwt.store

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 基于内存的 RefreshTokenStore 默认实现，清理策略同 [InMemoryTokenBlacklistStore]。
 */
class InMemoryRefreshTokenStore(
    private val clearInterval: Duration = Duration.ofMinutes(1)
) : RefreshTokenStore {

    private val log = KotlinLogging.logger {}

    private val entries = ConcurrentHashMap<String, RefreshToken>()

    private val lastCleanAt = AtomicLong(System.currentTimeMillis())

    override fun save(refreshToken: RefreshToken) {
        cleanIfNecessary()
        entries[refreshToken.jti] = refreshToken
    }

    override fun find(jti: String): RefreshToken? {
        val token = entries[jti] ?: return null
        if (token.expiresAt <= System.currentTimeMillis()) {
            entries.remove(jti)
            return null
        }
        return token
    }

    override fun revoke(jti: String) {
        if (entries.remove(jti) != null) {
            log.debug { "revoke refresh token, jti = $jti" }
        }
    }

    override fun revokeByUsername(username: String) {
        entries.entries.removeIf { it.value.username == username }
    }

    override fun removeExpired() {
        val now = System.currentTimeMillis()
        var cleared = 0
        val iterator = entries.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.expiresAt <= now) {
                iterator.remove()
                cleared++
            }
        }
        lastCleanAt.set(now)
        if (cleared > 0) {
            log.debug { "clean expired refresh token, cleared = $cleared, remaining = ${entries.size}" }
        }
    }

    private fun cleanIfNecessary() {
        val now = System.currentTimeMillis()
        if (now - lastCleanAt.get() >= clearInterval.toMillis()) {
            removeExpired()
        }
    }
}
package com.lhl.jwt.utils

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object CacheManager {

    private val logger = LoggerFactory.getLogger(CacheManager::class.java)
    private val cacheData = ConcurrentHashMap<String, CacheData<Any>>()
    const val ALWAYS_ACTIVE = 0

    @Suppress("UNCHECKED_CAST")
    fun <T> getData(key: String): T? {
        val data = cacheData[key] ?: return null
        return if (data.expire <= ALWAYS_ACTIVE || data.saveTime >= System.currentTimeMillis()) {
            data.data as T
        } else null
    }

    fun <T> setData(key: String, data: T, expire: Int) {
        cacheData[key] = CacheData(data, expire)
    }

    fun clear(key: String) {
        cacheData.remove(key)
    }

    fun clearAll() {
        cacheData.clear()
    }

    fun clearExpire() {
        logger.info("clear token cache starting, current cache size is {}", cacheData.size)
        val iterator = cacheData.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val cache = entry.value
            if (System.currentTimeMillis() - cache.saveTime >= 0) {
                iterator.remove()
                logger.info("clear token {}", entry.key)
            }
        }
        logger.info("clear token cache started, current cache size is {}", cacheData.size)
    }

    private class CacheData<T>(val data: T, expire: Int) {
        val saveTime: Long = System.currentTimeMillis() + if (expire <= 0) 0 else expire
        val expire: Long = if (expire <= 0) 0 else expire.toLong()
    }
}
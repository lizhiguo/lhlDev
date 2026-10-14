package com.lhl.jwt.utils

import cn.hutool.v7.json.JSONObject
import cn.hutool.v7.json.JSONUtil
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * 请求体解析工具：登录类过滤器需要在读取 body 前取出用户名 / 密码 / 验证码。
 */
object RequestBodyUtils {

    private val log = KotlinLogging.logger {}

    /** 解析 JSON 请求体，非法时返回 null */
    @JvmStatic
    fun toJson(body: String): JSONObject? {
        if (body.isBlank()) {
            return null
        }
        return try {
            JSONUtil.parseObj(body.trim())
        } catch (e: Exception) {
            log.debug { "parse request body failed: ${e.message}" }
            null
        }
    }
}
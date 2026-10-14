package com.lhl.jwt.sample.entity

import java.time.LocalDateTime

/**
 * 用户表。
 *
 * 仅出参使用，不接受客户端回传（见 [com.lhl.jwt.sample.controller.UserController]）。
 */
data class SysUser(
    var id: Long? = null,
    var username: String? = null,
    /** BCrypt 散列，任何时候都不要返回给前端 */
    var password: String? = null,
    var mobile: String? = null,
    var nickname: String? = null,
    var wxOpenId: String? = null,
    /** 1=正常，0=禁用 */
    var status: Int = STATUS_ENABLED,
    /** 0=正常，1=已删除 */
    var deleted: Int = NOT_DELETED,
    var createTime: LocalDateTime? = null,
    var updateTime: LocalDateTime? = null
) {

    /** 对外输出时剔除口令 */
    fun toBrief(): Map<String, Any?> = mapOf(
        "id" to id,
        "username" to username,
        "nickname" to nickname,
        "mobile" to mobile,
        "status" to status,
        "createTime" to createTime
    )

    companion object {
        const val STATUS_ENABLED = 1
        const val STATUS_DISABLED = 0
        const val NOT_DELETED = 0
        const val DELETED = 1
    }
}
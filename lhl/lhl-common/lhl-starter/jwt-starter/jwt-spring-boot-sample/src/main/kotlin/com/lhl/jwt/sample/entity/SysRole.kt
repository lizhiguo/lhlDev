package com.lhl.jwt.sample.entity

import java.time.LocalDateTime

/**
 * 角色 / 权限表。
 *
 * 角色与权限放在同一张表，区别只在于 [type]：
 * - [TYPE_ROLE]：`roleCode` 以 `ROLE_` 开头，对应 `hasRole('ADMIN')`
 * - [TYPE_PERMISSION]：`roleCode` 即权限标识，对应 `hasAuthority('USER_READ')`
 */
data class SysRole(
    var id: Long? = null,
    var roleCode: String? = null,
    var roleName: String? = null,
    /** 1=角色，2=权限 */
    var type: Int = TYPE_ROLE,
    var remark: String? = null,
    var createTime: LocalDateTime? = null
) {

    companion object {
        const val TYPE_ROLE = 1
        const val TYPE_PERMISSION = 2
    }
}
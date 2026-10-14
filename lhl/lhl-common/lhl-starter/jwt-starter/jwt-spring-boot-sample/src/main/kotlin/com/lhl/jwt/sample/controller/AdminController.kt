package com.lhl.jwt.sample.controller

import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.sample.service.UserAccountService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 角色维度鉴权演示：只有 ROLE_ADMIN 能访问。
 */
@RestController
class AdminController(
    private val userAccountService: UserAccountService
) {

    /** 需要管理员角色 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/roles")
    fun roles(): ApiResult<Any> = ApiResult.success(
        data = userAccountService.listRoles().map { mapOf("roleCode" to it.roleCode, "roleName" to it.roleName, "type" to it.type) }
    )
}
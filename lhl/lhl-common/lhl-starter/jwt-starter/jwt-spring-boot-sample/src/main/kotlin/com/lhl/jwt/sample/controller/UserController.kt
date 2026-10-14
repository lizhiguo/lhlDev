package com.lhl.jwt.sample.controller

import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.sample.entity.SysUser
import com.lhl.jwt.sample.service.UserAccountService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 权限校验演示。
 *
 * starter 已开启 @EnableMethodSecurity，所以细粒度权限直接用方法级注解表达，
 * 不需要改过滤器链：
 * - hasAuthority('USER_READ') 精确匹配 sys_role.role_code（type=2 的权限）
 * - hasRole('ADMIN') 自动补 ROLE_ 前缀，等价于 hasAuthority('ROLE_ADMIN')
 *
 * 未认证 -> 401（JwtAuthenticationEntryPoint）；已认证但缺权限 -> 403（JwtAccessDeniedHandler）。
 */
@RestController
@RequestMapping("/user")
class UserController(
    private val userAccountService: UserAccountService
) {

    /** 需要 USER_READ 权限 */
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/list")
    fun list(): ApiResult<Any> = ApiResult.success(
        data = userAccountService.listUsers().map(SysUser::toBrief)
    )

    /** 需要 USER_WRITE 权限 */
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PostMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Long, @RequestParam status: Int): ApiResult<Any> {
        if (status != SysUser.STATUS_ENABLED && status != SysUser.STATUS_DISABLED) {
            return ApiResult.fail(code = "400", msg = "status 只能是 0 或 1")
        }
        val affected = userAccountService.updateStatus(id, status)
        return if (affected > 0) {
            ApiResult.success(data = mapOf("id" to id, "status" to status))
        } else {
            ApiResult.fail(code = "404", msg = "用户不存在: $id")
        }
    }

    /** 需要 USER_DELETE 权限；逻辑删除，保留行便于审计 */
    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ApiResult<Any> {
        val affected = userAccountService.logicDelete(id)
        return if (affected > 0) {
            ApiResult.success(data = mapOf("id" to id, "deleted" to true))
        } else {
            ApiResult.fail(code = "404", msg = "用户不存在: $id")
        }
    }
}
package com.lhl.jwt.sample

import com.lhl.jwt.sample.entity.SysRole
import com.lhl.jwt.sample.entity.SysUser
import com.lhl.jwt.sample.mapper.SysRoleMapper
import com.lhl.jwt.sample.mapper.SysUserMapper
import com.lhl.jwt.sample.mapper.SysUserRoleMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 演示数据初始化。
 *
 * 口令必须是 BCrypt 散列，没法写在 schema.sql 里，所以启动时用 PasswordEncoder
 * 编码后幂等插入：账号已存在则跳过，重复启动不会重复插入、也不会重置口令。
 *
 * 演示矩阵：
 * | 账号     | 口令        | 角色/权限                            |
 * |----------|-------------|--------------------------------------|
 * | admin    | admin123    | ROLE_ADMIN + 全部权限                |
 * | lhl      | lhl123      | ROLE_USER + USER_READ                |
 * | guest    | guest123    | 无（用于观察 403）                   |
 * | blocked  | blocked123  | ROLE_USER，但 status=0（登录被拒）    |
 */
@Component
class SampleDataInitializer(
    private val userMapper: SysUserMapper,
    private val roleMapper: SysRoleMapper,
    private val userRoleMapper: SysUserRoleMapper,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    private val log = KotlinLogging.logger {}

    @Transactional
    override fun run(args: ApplicationArguments) {
        seedRoles()
        seedUser("admin", "admin123", "13800000001", "管理员", "wx_openid_admin", listOf(ROLE_ADMIN, USER_READ, USER_WRITE, USER_DELETE))
        seedUser("lhl", "lhl123", "13800000002", "普通用户", "wx_openid_lhl", listOf(ROLE_USER, USER_READ))
        seedUser("guest", "guest123", "13800000003", "访客", null, emptyList())
        seedUser("blocked", "blocked123", "13800000004", "已禁用", null, listOf(ROLE_USER), SysUser.STATUS_DISABLED)
        log.info { "demo data ready: admin/admin123 lhl/lhl123 guest/guest123 blocked/blocked123" }
    }

    /** 角色 type=1，权限 type=2 */
    private fun seedRoles() {
        listOf(
            SysRole(roleCode = ROLE_ADMIN, roleName = "管理员", type = SysRole.TYPE_ROLE, remark = "可访问全部端点"),
            SysRole(roleCode = ROLE_USER, roleName = "普通用户", type = SysRole.TYPE_ROLE, remark = "普通角色"),
            SysRole(roleCode = USER_READ, roleName = "查询用户", type = SysRole.TYPE_PERMISSION, remark = "hasAuthority"),
            SysRole(roleCode = USER_WRITE, roleName = "编辑用户", type = SysRole.TYPE_PERMISSION, remark = "hasAuthority"),
            SysRole(roleCode = USER_DELETE, roleName = "删除用户", type = SysRole.TYPE_PERMISSION, remark = "hasAuthority")
        ).forEach { role ->
            val code = role.roleCode!!
            if (roleMapper.selectIdByCode(code) == null) {
                roleMapper.insert(role)
            }
        }
    }

    private fun seedUser(
        username: String,
        rawPassword: String,
        mobile: String,
        nickname: String,
        wxOpenId: String?,
        roleCodes: List<String>,
        status: Int = SysUser.STATUS_ENABLED
    ) {
        if (userMapper.countByUsername(username) > 0) {
            return
        }
        val user = SysUser(
            username = username,
            password = passwordEncoder.encode(rawPassword),
            mobile = mobile,
            nickname = nickname,
            wxOpenId = wxOpenId,
            status = status
        )
        userMapper.insert(user)
        roleCodes.forEach { code ->
            roleMapper.selectIdByCode(code)?.let { roleId ->
                userRoleMapper.insert(user.id!!, roleId)
            }
        }
    }

    private companion object {
        const val ROLE_ADMIN = "ROLE_ADMIN"
        const val ROLE_USER = "ROLE_USER"
        const val USER_READ = "USER_READ"
        const val USER_WRITE = "USER_WRITE"
        const val USER_DELETE = "USER_DELETE"
    }
}
package com.lhl.jwt.sample.service

import com.lhl.jwt.JwtUser
import com.lhl.jwt.domain.bo.WxExtBO
import com.lhl.jwt.sample.entity.SysUser
import com.lhl.jwt.sample.mapper.SysRoleMapper
import com.lhl.jwt.sample.mapper.SysUserMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

/**
 * 用户账号服务：把 MySQL 数据组装成 jwt-starter 的认证主体 [JwtUser]。
 *
 * 这是 starter 接入业务数据源的唯一必需环节，DbUserDetailsService 只是它的 SPI 适配层。
 */
@Service
class UserAccountService(
    private val userMapper: SysUserMapper,
    private val roleMapper: SysRoleMapper
) {

    private val log = KotlinLogging.logger {}

    fun loadByUsername(username: String): JwtUser? = build(userMapper.selectByUsername(username))

    fun loadByMobile(mobile: String): JwtUser? = build(userMapper.selectByMobile(mobile))

    fun loadByOpenId(openId: String): JwtUser? = build(userMapper.selectByWxOpenId(openId))

    fun loadById(userId: String): JwtUser? = userId.toLongOrNull()?.let { build(userMapper.selectById(it)) }

    /** 原始用户记录，供业务端点展示非敏感字段 */
    fun findUser(username: String): SysUser? = userMapper.selectByUsername(username)

    fun listUsers() = userMapper.selectList()

    fun listRoles() = roleMapper.selectList()

    fun updateStatus(id: Long, status: Int): Int = userMapper.updateStatus(id, status)

    fun logicDelete(id: Long): Int = userMapper.logicDelete(id)

    /**
     * 组装认证主体。
     *
     * @return 用户不存在时返回 null，由调用方转成 UsernameNotFoundException
     * @throws DisabledException 账号被禁用，让登录在口令比对之前就失败
     */
    private fun build(user: SysUser?): JwtUser? {
        if (user == null) {
            return null
        }
        if (user.status != SysUser.STATUS_ENABLED) {
            log.warn { "account disabled, username = ${user.username}" }
            throw DisabledException("账号已被禁用: ${user.username}")
        }
        val id = user.id ?: throw IllegalStateException("sys_user.id 不能为空")
        val authorities = roleMapper.selectByUserId(id).mapNotNull { role ->
            role.roleCode?.takeIf { it.isNotBlank() }?.let(::SimpleGrantedAuthority)
        }
        return JwtUser(user.username.orEmpty(), user.password.orEmpty(), authorities).apply {
            userId = id.toString()
            wxExtBO = user.wxOpenId?.let { WxExtBO(openid = it) }
        }
    }
}
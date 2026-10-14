package com.lhl.jwt.sample

import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.sample.service.UserAccountService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * jwt-starter 的用户查询 SPI，数据来自 MySQL。
 *
 * 三种登录方式的入口在这里汇合：
 * - 账号密码：loadUserByUsername（DaoAuthenticationProvider）
 * - 手机验证码：loadUserByMobile（MobileCodeAuthenticationProvider）
 * - 微信：loadUserByOpenId（WxAuthenticationProvider）
 *
 * 口令比对交给 starter 装配的 DaoAuthenticationProvider + BCryptPasswordEncoder，
 * 这里只负责「查到用户并带上角色/权限」，不要自己比对口令。
 */
@Service
class SampleUserDetailsService(
    private val userAccountService: UserAccountService
) : JwtUserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userAccountService.loadByUsername(username)
            ?: throw UsernameNotFoundException("用户不存在: $username")

    override fun loadUserByMobile(mobile: String): UserDetails =
        userAccountService.loadByMobile(mobile)
            ?: throw UsernameNotFoundException("手机号未绑定账号: $mobile")

    override fun loadUserByOpenId(openId: String): UserDetails =
        userAccountService.loadByOpenId(openId)
            ?: throw UsernameNotFoundException("微信未绑定账号: $openId")

    override fun loadUserByUserId(userId: String): UserDetails =
        userAccountService.loadById(userId)
            ?: throw UsernameNotFoundException("用户不存在: $userId")
}
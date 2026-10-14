package com.lhl.jwt.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * 用户查询 SPI。
 *
 * 业务方实现本接口并注册为 Bean，jwt-starter 的过滤器 / Provider 即可完成认证闭环。
 *
 * 未实现的登录方式默认抛出 UsernameNotFoundException，因此不会被意外认证通过。
 */
interface JwtUserDetailsService : UserDetailsService {

    /** 手机号登录 */
    fun loadUserByMobile(mobile: String): UserDetails =
        throw UsernameNotFoundException("JwtUserDetailsService#loadUserByMobile 未实现")

    /** 微信登录：公众号 / 小程序 openId */
    fun loadUserByOpenId(openId: String): UserDetails =
        throw UsernameNotFoundException("JwtUserDetailsService#loadUserByOpenId 未实现")

    /** 按业务主键查询 */
    fun loadUserByUserId(userId: String): UserDetails =
        throw UsernameNotFoundException("JwtUserDetailsService#loadUserByUserId 未实现")

    /**
     * 账号密码登录。
     *
     * 用户不存在时必须抛出 UsernameNotFoundException。
     */
    override fun loadUserByUsername(username: String): UserDetails =
        throw UsernameNotFoundException("JwtUserDetailsService#loadUserByUsername 未实现")
}
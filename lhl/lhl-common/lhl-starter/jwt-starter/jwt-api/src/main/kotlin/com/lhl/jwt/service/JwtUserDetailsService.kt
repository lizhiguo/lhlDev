package com.lhl.jwt.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

interface JwtUserDetailsService : UserDetailsService {

    fun loadUserByMobile(mobile: String): UserDetails? {
        return null
    }

    fun loadUserByOpenId(openId: String): UserDetails? {
        return null
    }

    fun loadUserByUserId(userId: String): UserDetails? {
        return null
    }

    override fun loadUserByUsername(username: String): UserDetails? {
        return null
    }
}
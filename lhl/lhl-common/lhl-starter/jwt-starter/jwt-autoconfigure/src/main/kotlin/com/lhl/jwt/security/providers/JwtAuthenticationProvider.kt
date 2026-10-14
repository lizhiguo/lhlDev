package com.lhl.jwt.security.providers

import com.lhl.jwt.exception.JwtParseException
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.token.JwtAuthenticationToken
import com.lhl.jwt.utils.JwtConstant
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

/**
 * JWT 认证 Provider。
 *
 * 校验链路：签名 / exp / iss / aud / tokenType（由 [JwtTokenService] 完成）-> 查询用户 -> 构建已认证令牌。
 * 用户不存在时抛出 UsernameNotFoundException，最终由失败处理器降级为 401。
 */
class JwtAuthenticationProvider(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: JwtUserDetailsService
) : AuthenticationProvider {

    private val log = KotlinLogging.logger {}

    override fun authenticate(authentication: Authentication): Authentication {
        val token = (authentication as JwtAuthenticationToken).token
            ?: throw JwtParseException(JwtConstant.TOKEN_NOT_EMPTY)
        val payload = jwtTokenService.parse(token)
        val username = payload.sub
            ?: throw JwtParseException(JwtConstant.TOKEN_PARSE_ERROR)
        log.debug { "jwt authenticated, user = $username" }
        val userDetails = userDetailsService.loadUserByUsername(username)
        return JwtAuthenticationToken(userDetails, token, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean =
        JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
}
package com.lhl.jwt.handler

import com.lhl.jwt.exception.JwtSignatureVerifyException
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.JwtUtils
import com.nimbusds.jose.JOSEException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class LoginSuccessHandler(
    private val jwtUserDetailsService: JwtUserDetailsService
) : AuthenticationSuccessHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val user = authentication.principal as UserDetails
        val token = generateToken(user, user.username)
        response.setHeader(JwtConstant.AUTHORIZATION_HEADER, JwtConstant.AUTHORIZATION_START_STRING + token)
        response.setHeader(JwtConstant.ACCESS_CONTROL_EXPOSE_HEADER, JwtConstant.AUTHORIZATION_HEADER)
        log.info { "Login success! token : $token" }
    }

    private fun generateToken(user: UserDetails, salt: String): String {
        return try {
            JwtUtils.createToken(user.username, user.authorities, salt)
        } catch (e: JOSEException) {
            throw JwtSignatureVerifyException("生成签名异常")
        }
    }
}
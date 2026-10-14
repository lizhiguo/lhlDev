package com.lhl.jwt.autoconfigure

import com.lhl.jwt.handler.JwtAccessDeniedHandler
import com.lhl.jwt.handler.JwtAuthenticationEntryPoint
import com.lhl.jwt.handler.JwtAuthenticationFailureHandler
import com.lhl.jwt.handler.JwtRefreshSuccessHandler
import com.lhl.jwt.handler.LoginSuccessHandler
import com.lhl.jwt.handler.LogoutBlacklistHandler
import com.lhl.jwt.handler.TokenClearLogoutHandler
import com.lhl.jwt.security.providers.JwtAuthenticationProvider
import com.lhl.jwt.security.providers.MobileCodeAuthenticationProvider
import com.lhl.jwt.security.providers.WxAuthenticationProvider
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.service.MobileCodeValidator
import com.lhl.jwt.store.InMemoryRefreshTokenStore
import com.lhl.jwt.store.InMemoryTokenBlacklistStore
import com.lhl.jwt.store.RefreshTokenStore
import com.lhl.jwt.store.TokenBlacklistStore
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * jwt-starter 核心组件装配。
 *
 * 触发条件：Web 应用 + Spring Security 在 classpath。
 * 所有 Bean 都带 `@ConditionalOnMissingBean`，业务方可以逐个替换为自定义实现。
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "jwt.security", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JwtProperties::class)
@EnableMethodSecurity
@Import(MobileCodeValidatorAutoConfiguration::class, JwtWebMvcAutoConfiguration::class)
class JwtSecurityAutoConfiguration {

    // ---------------------------------------------------------------- 基础组件

    @Bean
    @ConditionalOnMissingBean
    fun jwtTokenService(jwtProperties: JwtProperties, refreshTokenStore: RefreshTokenStore): JwtTokenService =
        JwtTokenService(jwtProperties.toSettings(), refreshTokenStore)

    @Bean
    @ConditionalOnMissingBean
    fun tokenBlacklistStore(jwtProperties: JwtProperties): TokenBlacklistStore =
        InMemoryTokenBlacklistStore(jwtProperties.clearInterval)

    @Bean
    @ConditionalOnMissingBean
    fun refreshTokenStore(jwtProperties: JwtProperties): RefreshTokenStore =
        InMemoryRefreshTokenStore(jwtProperties.clearInterval)

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder::class)
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    // ---------------------------------------------------------------- 认证提供者

    @Bean
    @ConditionalOnMissingBean(name = ["jwtAuthenticationProvider"])
    fun jwtAuthenticationProvider(
        jwtTokenService: JwtTokenService,
        jwtUserDetailsService: JwtUserDetailsService
    ): AuthenticationProvider = JwtAuthenticationProvider(jwtTokenService, jwtUserDetailsService)

    @Bean
    @ConditionalOnMissingBean(name = ["daoAuthenticationProvider"])
    fun daoAuthenticationProvider(
        jwtUserDetailsService: JwtUserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider = DaoAuthenticationProvider(jwtUserDetailsService).apply {
        setPasswordEncoder(passwordEncoder)
    }

    @Bean
    @ConditionalOnMissingBean(name = ["wxAuthenticationProvider"])
    fun wxAuthenticationProvider(
        jwtUserDetailsService: JwtUserDetailsService
    ): AuthenticationProvider = WxAuthenticationProvider(jwtUserDetailsService)

    @Bean
    @ConditionalOnMissingBean(name = ["mobileAuthenticationProvider"])
    fun mobileAuthenticationProvider(
        jwtUserDetailsService: JwtUserDetailsService,
        mobileCodeValidator: MobileCodeValidator
    ): AuthenticationProvider = MobileCodeAuthenticationProvider(jwtUserDetailsService, mobileCodeValidator)

    // ---------------------------------------------------------------- 处理器

    @Bean
    @ConditionalOnMissingBean
    fun loginSuccessHandler(jwtTokenService: JwtTokenService): LoginSuccessHandler =
        LoginSuccessHandler(jwtTokenService)

    @Bean
    @ConditionalOnMissingBean
    fun jwtRefreshSuccessHandler(jwtTokenService: JwtTokenService): JwtRefreshSuccessHandler =
        JwtRefreshSuccessHandler(jwtTokenService)

    @Bean
    @ConditionalOnMissingBean
    fun jwtAuthenticationFailureHandler(): JwtAuthenticationFailureHandler = JwtAuthenticationFailureHandler()

    @Bean
    @ConditionalOnMissingBean
    fun jwtAuthenticationEntryPoint(): JwtAuthenticationEntryPoint = JwtAuthenticationEntryPoint()

    @Bean
    @ConditionalOnMissingBean
    fun jwtAccessDeniedHandler(): JwtAccessDeniedHandler = JwtAccessDeniedHandler()

    @Bean
    @ConditionalOnMissingBean
    fun tokenClearLogoutHandler(jwtTokenService: JwtTokenService): TokenClearLogoutHandler =
        TokenClearLogoutHandler(jwtTokenService)

    @Bean
    @ConditionalOnMissingBean
    fun logoutBlacklistHandler(
        jwtTokenService: JwtTokenService,
        tokenBlacklistStore: TokenBlacklistStore
    ): LogoutBlacklistHandler = LogoutBlacklistHandler(jwtTokenService, tokenBlacklistStore)
}
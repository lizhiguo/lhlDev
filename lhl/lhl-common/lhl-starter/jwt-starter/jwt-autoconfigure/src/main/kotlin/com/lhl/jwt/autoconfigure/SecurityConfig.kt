package com.lhl.jwt.autoconfigure


import com.lhl.jwt.security.configs.JwtAuthenticationConfigurer
import com.lhl.jwt.security.configs.LoginConfigurer
import com.lhl.jwt.security.configs.MobileConfigurer
import com.lhl.jwt.security.configs.WxConfigurer
import com.lhl.jwt.security.filters.OptionsRequestFilter
import com.lhl.jwt.security.providers.JwtAuthenticationProvider
import com.lhl.jwt.security.providers.MobileCodeAuthenticationProvider
import com.lhl.jwt.security.providers.WxAuthenticationProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsUtils

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(JwtProperties::class)
open class SecurityConfig(
    private val userDetailsService: JwtUserDetailsService,
    private val jwtProperties: JwtProperties
) {

    companion object {
        private const val DEFAULT_PERMITURL = "/mobileCodeLogin/**,/wxLogin/**,/login/**,/logout/**"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun jwtRefreshSuccessHandler(): JwtRefreshSuccessHandler {
        val handler = JwtRefreshSuccessHandler(userDetailsService)
        handler.tokenRefreshInterval = jwtProperties.tokenRefreshInterval
        return handler
    }

    @Bean
    @ConditionalOnMissingBean
    open fun loginSuccessHandler(): LoginSuccessHandler = LoginSuccessHandler(userDetailsService)

    @Bean
    @ConditionalOnMissingBean
    open fun jwtAuthenticationFailureHandler(): JwtAuthenticationFailureHandler = JwtAuthenticationFailureHandler()

    @Bean
    @ConditionalOnMissingBean
    open fun tokenClearLogoutHandler(): TokenClearLogoutHandler = TokenClearLogoutHandler(userDetailsService)

    @Bean
    @ConditionalOnMissingBean
    open fun logoutBlacklistHandler(): LogoutBlacklistHandler = LogoutBlacklistHandler()

    @Bean("jwtAuthenticationProvider")
    open fun jwtAuthenticationProvider(): AuthenticationProvider = JwtAuthenticationProvider(userDetailsService)

    @Bean("daoAuthenticationProvider")
    open fun daoAuthenticationProvider(): AuthenticationProvider {
        val daoProvider = DaoAuthenticationProvider()
        daoProvider.setUserDetailsService(userDetailsService)
        daoProvider.setPasswordEncoder(BCryptPasswordEncoder())
        return daoProvider
    }

    @Bean("wxAuthenticationProvider")
    open fun wxAuthenticationProvider(): AuthenticationProvider = WxAuthenticationProvider(userDetailsService)

    @Bean("mobileAuthenticationProvider")
    open fun mobileAuthenticationProvider(): AuthenticationProvider = MobileCodeAuthenticationProvider(userDetailsService)

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val urls = (jwtProperties.permitUrls?.let { "$it,$DEFAULT_PERMITURL" } ?: DEFAULT_PERMITURL)
            .split(",")
            .map { it.trim() }
            .toTypedArray()

        http {
            authenticationProvider { jwtAuthenticationProvider() }
            authenticationProvider { daoAuthenticationProvider() }
            authenticationProvider { wxAuthenticationProvider() }
            authenticationProvider { mobileAuthenticationProvider() }

            cors { }
            csrf { disable() }

            authorizeHttpRequests {
                authorize(CorsUtils::isPreFlightRequest, permitAll)
                for (url in urls) {
                    authorize(url, permitAll)
                }
                authorize(anyRequest, authenticated)
            }

            formLogin { disable() }

            addFilterAfter<org.springframework.web.filter.CorsFilter>(OptionsRequestFilter())
            apply(LoginConfigurer(userDetailsService).apply {
                authenticationSuccessHandler(loginSuccessHandler())
                authenticationFailureHandler(jwtAuthenticationFailureHandler())
            })

            addFilterAfter<org.springframework.web.filter.CorsFilter>(OptionsRequestFilter())
            apply(WxConfigurer().apply {
                authenticationSuccessHandler(loginSuccessHandler())
                authenticationFailureHandler(jwtAuthenticationFailureHandler())
            })

            addFilterAfter<org.springframework.web.filter.CorsFilter>(OptionsRequestFilter())
            apply(MobileConfigurer(userDetailsService).apply {
                authenticationSuccessHandler(loginSuccessHandler())
                authenticationFailureHandler(jwtAuthenticationFailureHandler())
            })

            apply(JwtAuthenticationConfigurer().apply {
                authenticationSuccessHandler(jwtRefreshSuccessHandler())
                authenticationFailureHandler(jwtAuthenticationFailureHandler())
                permissiveRequestUrls(*urls)
            })

            logout {
                addLogoutHandler(tokenClearLogoutHandler())
                addLogoutHandler(logoutBlacklistHandler())
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler()
            }

            sessionManagement { disable() }
        }

        return http.build()
    }
}
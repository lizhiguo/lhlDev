package com.lhl.jwt.autoconfigure

import com.lhl.jwt.handler.JwtAccessDeniedHandler
import com.lhl.jwt.handler.JwtAuthenticationEntryPoint
import com.lhl.jwt.handler.JwtAuthenticationFailureHandler
import com.lhl.jwt.handler.JwtRefreshSuccessHandler
import com.lhl.jwt.handler.LoginSuccessHandler
import com.lhl.jwt.handler.LogoutBlacklistHandler
import com.lhl.jwt.handler.TokenClearLogoutHandler
import com.lhl.jwt.security.configs.JwtAuthenticationConfigurer
import com.lhl.jwt.security.configs.LoginConfigurer
import com.lhl.jwt.security.configs.MobileConfigurer
import com.lhl.jwt.security.configs.WxConfigurer
import com.lhl.jwt.security.filters.JwtAuthenticationFilter
import com.lhl.jwt.security.filters.LoginAuthenticationFilter
import com.lhl.jwt.security.filters.MobileCodeAuthenticationFilter
import com.lhl.jwt.security.filters.OptionsRequestFilter
import com.lhl.jwt.security.filters.WxAuthenticationFilter
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.service.JwtUserDetailsService
import com.lhl.jwt.store.TokenBlacklistStore
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
/**
 * jwt-starter 安全过滤器链。
 *
 * 过滤器顺序（自上而下）：
 * 1. CorsFilter
 * 2. OptionsRequestFilter           —— 放行 OPTIONS 预检
 * 3. LoginAuthenticationFilter      —— POST 登录端点
 * 4. MobileCodeAuthenticationFilter —— POST 手机验证码登录端点
 * 5. WxAuthenticationFilter         —— POST 微信登录端点
 * 6. LogoutFilter                   —— POST 登出端点
 * 7. JwtAuthenticationFilter        —— 携带 token 的请求
 * 8. FilterSecurityInterceptor      —— 授权判定
 */
@AutoConfiguration(after = [JwtSecurityAutoConfiguration::class])
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "jwt.security", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
class JwtSecurityFilterChainAutoConfiguration {

    private val log = KotlinLogging.logger {}

    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource::class)
    @ConditionalOnProperty(prefix = "jwt.security.cors", name = ["enabled"], havingValue = "true")
    fun corsConfigurationSource(jwtProperties: JwtProperties): CorsConfigurationSource {
        val cors = jwtProperties.cors
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = cors.allowedOriginPatterns
            allowedMethods = cors.allowedMethods
            allowedHeaders = cors.allowedHeaders
            exposedHeaders = cors.exposedHeaders
            allowCredentials = cors.allowCredentials
            maxAge = cors.maxAge.seconds
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
    @Bean
    @ConditionalOnMissingBean(WebSecurityCustomizer::class)
    fun webSecurityCustomizer(jwtProperties: JwtProperties): WebSecurityCustomizer {
        val ignoreUrls = jwtProperties.ignoreUrls.map { it.trim() }.filter { it.isNotEmpty() }
        if (ignoreUrls.isNotEmpty()) {
            log.info { "jwt starter ignoring urls = $ignoreUrls" }
        }
        // 等价于旧版 WebSecurityConfigurerAdapter#configure(WebSecurity web)
        return WebSecurityCustomizer { web ->
            if (ignoreUrls.isNotEmpty()) {
                web.ignoring().requestMatchers(*ignoreUrls.toTypedArray())
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = ["jwtSecurityFilterChain"])
    fun jwtSecurityFilterChain(
        http: HttpSecurity,
        jwtProperties: JwtProperties,
        jwtTokenService: JwtTokenService,
        tokenBlacklistStore: TokenBlacklistStore,
        jwtUserDetailsService: JwtUserDetailsService,
        loginSuccessHandler: LoginSuccessHandler,
        jwtRefreshSuccessHandler: JwtRefreshSuccessHandler,
        jwtAuthenticationFailureHandler: JwtAuthenticationFailureHandler,
        jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
        jwtAccessDeniedHandler: JwtAccessDeniedHandler,
        tokenClearLogoutHandler: TokenClearLogoutHandler,
        logoutBlacklistHandler: LogoutBlacklistHandler,
        authenticationProviders: List<AuthenticationProvider>,
        corsConfigurationSource: ObjectProvider<CorsConfigurationSource>
    ): SecurityFilterChain {
        val settings = jwtTokenService.configuration
        val permitUrls = jwtProperties.resolvePermitUrls()
        // 收集容器中所有 AuthenticationProvider Bean（Dao / Jwt / Wx / Mobile）
        // 自建 ProviderManager：容器中存在 JwtUserDetailsService 时，AuthenticationConfiguration 会退化为
        // 仅使用 DaoAuthenticationProvider，导致 token 认证失败，因此这里显式组装全部 Provider。
        val authenticationManager = ProviderManager(*authenticationProviders.toTypedArray())
        authenticationProviders.forEach { http.authenticationProvider(it) }
        log.info {
            "jwt starter enabled, permit urls = $permitUrls, providers = " +
                authenticationProviders.map { it::class.simpleName } +
                (if (jwtProperties.securityMatchers.isEmpty()) "" else ", matchers = ${jwtProperties.securityMatchers}")
        }

        // 限定本链的作用域，便于业务方再注册其它 SecurityFilterChain（如有状态的页面链）
        jwtProperties.securityMatchers
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.isNotEmpty() }
            ?.let { http.securityMatcher(*it.toTypedArray()) }

        http {
            corsConfigurationSource.ifAvailable { source -> cors { configurationSource = source } }
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            logout { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            exceptionHandling {
                authenticationEntryPoint = jwtAuthenticationEntryPoint
                accessDeniedHandler = jwtAccessDeniedHandler
            }
            authorizeHttpRequests {
                permitUrls.forEach { authorize(it, permitAll) }
                authorize(anyRequest, authenticated)
            }
        }

        http.addFilterBefore(OptionsRequestFilter(), UsernamePasswordAuthenticationFilter::class.java)
        http.apply(
            LoginConfigurer<HttpSecurity>(LoginAuthenticationFilter(settings.loginPath)).let { configurer ->
                configurer.authenticationManager(authenticationManager)
                    .authenticationSuccessHandler(loginSuccessHandler)
                    .authenticationFailureHandler(jwtAuthenticationFailureHandler)
                configurer
            }
        )
        http.apply(
            MobileConfigurer<HttpSecurity>(MobileCodeAuthenticationFilter(settings.mobileLoginPath)).let { configurer ->
                configurer.authenticationManager(authenticationManager)
                    .authenticationSuccessHandler(loginSuccessHandler)
                    .authenticationFailureHandler(jwtAuthenticationFailureHandler)
                configurer
            }
        )
        http.apply(
            WxConfigurer<HttpSecurity>(WxAuthenticationFilter(settings.wxLoginPath)).let { configurer ->
                configurer.authenticationManager(authenticationManager)
                    .authenticationSuccessHandler(loginSuccessHandler)
                    .authenticationFailureHandler(jwtAuthenticationFailureHandler)
                configurer
            }
        )
        http.apply(
            JwtAuthenticationConfigurer<HttpSecurity>(
                JwtAuthenticationFilter(jwtTokenService, tokenBlacklistStore)
            ).let { configurer ->
                configurer.authenticationManager(authenticationManager)
                    .authenticationSuccessHandler(jwtRefreshSuccessHandler)
                    .authenticationFailureHandler(jwtAuthenticationFailureHandler)
                    .permissiveRequestUrls(*permitUrls.toTypedArray())
                configurer
            }
        )

        http.logout { logout ->
            logout.logoutUrl(settings.logoutPath)
            logout.addLogoutHandler(tokenClearLogoutHandler)
            logout.addLogoutHandler(logoutBlacklistHandler)
            logout.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler())
            logout.clearAuthentication(true)
            logout.invalidateHttpSession(false)
            settings.cookieName?.let { logout.deleteCookies(it) }
        }

        return http.build()
    }
}
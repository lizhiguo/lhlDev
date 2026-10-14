package com.lhl.jwt.autoconfigure

import com.lhl.jwt.resolver.LoginUserHandlerMethodArgumentResolver
import com.lhl.jwt.resolver.UserContextCleanupFilter
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.service.JwtUserDetailsService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 注册 `@CurrentUser` 参数解析器与 UserContext 清理过滤器。
 *
 * 仅在 Spring MVC 应用中生效；WebFlux 应用请自行实现参数解析。
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(WebMvcConfigurer::class)
@ConditionalOnProperty(prefix = "jwt.security", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class JwtWebMvcAutoConfiguration(
    private val jwtTokenService: JwtTokenService,
    private val jwtUserDetailsService: JwtUserDetailsService
) : WebMvcConfigurer {

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(LoginUserHandlerMethodArgumentResolver(jwtTokenService, jwtUserDetailsService))
    }

    @Bean
    @ConditionalOnMissingBean(UserContextCleanupFilter::class)
    fun userContextCleanupFilter(): OncePerRequestFilter = UserContextCleanupFilter()
}
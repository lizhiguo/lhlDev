package com.lhl.jwt.autoconfigure

import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.service.JwtUserDetailsService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 * 内置刷新令牌端点：`POST /refresh`。
 *
 * 请求体形如 `{ "refreshToken": "xxx" }`，返回新的 access / refresh token 对。
 * 不需要时把 `jwt.security.endpoint-enabled` 设为 false 关闭；
 * 需要自定义路径时，继承 [JwtTokenController] 并覆盖它。
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(
    prefix = "jwt.security",
    name = ["enabled", "endpoint-enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class JwtTokenEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtTokenController::class)
    fun jwtTokenController(
        jwtTokenService: JwtTokenService,
        jwtUserDetailsService: JwtUserDetailsService
    ): JwtTokenController = JwtTokenController(jwtTokenService, jwtUserDetailsService)
}

@RestController
class JwtTokenController(
    private val jwtTokenService: JwtTokenService,
    private val jwtUserDetailsService: JwtUserDetailsService
) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/refresh")
    fun refresh(@RequestBody(required = false) request: Map<String, String?>?): ApiResult<Any> {
        val refreshToken = request?.get("refreshToken")
        if (refreshToken.isNullOrBlank()) {
            return ApiResult.fail(code = "400", msg = "refreshToken must not be empty")
        }
        return try {
            log.debug { "refresh token request" }
            ApiResult.success(data = jwtTokenService.refresh(refreshToken, jwtUserDetailsService))
        } catch (e: Exception) {
            ApiResult.fail(code = "401", msg = e.message ?: "refresh token 无效")
        }
    }
}
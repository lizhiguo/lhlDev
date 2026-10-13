package com.lhl.common.api.autoconfigure

import com.lhl.common.api.config.ApiFilterConfig
import com.lhl.common.api.advice.ApiResponseDataAdvice
import com.lhl.common.api.properties.ApiProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(ApiResponseDataAdvice::class)
@EnableConfigurationProperties(ApiProperties::class)
class ApiAutoConfiguration(val apiProperties: ApiProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun apiConfig(): ApiFilterConfig {
        return ApiFilterConfig(
            adviceFilterPackage = apiProperties.adviceFilterPackage,
            adviceFilterClass = apiProperties.adviceFilterClass,
            adviceFilterStaticPath = apiProperties.adviceFilterStaticPath
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun greeter(): ApiResponseDataAdvice {
        return ApiResponseDataAdvice(apiConfig())
    }
}
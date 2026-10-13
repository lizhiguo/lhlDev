package com.lhl.common.api.config

import com.lhl.common.api.converter.StringToNullConverter
import com.lhl.common.core.helper.JacksonHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tools.jackson.databind.ObjectMapper

/**
 * 配置转换器
 */
@Configuration
class WebConfig: WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToNullConverter())
    }
}
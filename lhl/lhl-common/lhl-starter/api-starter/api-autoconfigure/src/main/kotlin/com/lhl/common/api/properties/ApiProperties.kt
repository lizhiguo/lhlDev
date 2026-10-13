package com.lhl.common.api.properties

import com.lhl.common.api.properties.ApiProperties.Companion.PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = PREFIX)
data class ApiProperties(
    /**
     * 统一返回过滤包
     */
    var adviceFilterPackage: MutableSet<String>? = null,

    /**
     * 统一返回过滤类
     */
    val adviceFilterClass: MutableSet<String>? = null,

    /**
     * 静态资源路径
     */
    var adviceFilterStaticPath: MutableSet<String>? = mutableSetOf(
        "/swagger-resources",
        "/webjars",
        "/v3/api-docs",
        "/swagger-ui.html",
        "/favicon.ico"
    )
) {
    companion object {
        const val PREFIX = "api.filter"
    }
}
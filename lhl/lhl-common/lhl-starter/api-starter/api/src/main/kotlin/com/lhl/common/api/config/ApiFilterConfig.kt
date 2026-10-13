package com.lhl.common.api.config

/***
 * 统一返回过滤配置
 *
 * @author zhaolp
 * @date 2020/08/05
 */
data class ApiFilterConfig(
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
    var adviceFilterStaticPath: MutableSet<String>? = null
)
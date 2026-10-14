package com.lhl.jwt.service

/**
 * 短信验证码校验 SPI。
 *
 * 手机号登录时由 [com.lhl.jwt.security.providers.MobileCodeAuthenticationProvider] 调用，
 * 业务方注册自己的实现即可接入既有短信中心。
 */
fun interface MobileCodeValidator {

    /**
     * 校验验证码。
     *
     * @param code 用户提交的验证码
     * @return true 表示校验通过；false 表示验证码错误 / 过期
     */
    fun isValid(mobile: String, code: String): Boolean
}
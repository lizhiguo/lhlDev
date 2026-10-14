package com.lhl.jwt.autoconfigure

import com.lhl.jwt.service.MobileCodeValidator
import com.lhl.jwt.utils.CacheManager
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.time.Duration

/**
 * 短信验证码校验的默认实现。
 *
 * 验证码由业务侧写入内存缓存（key = `sms:code:{mobile}`），校验通过即消费（一次性）。
 * 生产环境建议注册自己的 [MobileCodeValidator] 接入既有短信中心 / Redis。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "jwt.security", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JwtProperties::class)
class MobileCodeValidatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MobileCodeValidator::class)
    fun mobileCodeValidator(jwtProperties: JwtProperties): MobileCodeValidator =
        DefaultMobileCodeValidator(jwtProperties.clearInterval)

    class DefaultMobileCodeValidator(clearInterval: Duration) : MobileCodeValidator {

        override fun isValid(mobile: String, code: String): Boolean {
            val key = keyOf(mobile)
            val cached = CacheManager.getData<String>(key)
            if (cached == null || cached != code) {
                return false
            }
            CacheManager.clear(key)
            return true
        }

        companion object {

            @JvmStatic
            fun keyOf(mobile: String): String = "sms:code:$mobile"

            @JvmStatic
            fun save(mobile: String, code: String, ttl: Duration) {
                CacheManager.setData(keyOf(mobile), code, ttl.toMillis())
            }
        }
    }
}
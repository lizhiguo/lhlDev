package com.lhl.jwt.sample.controller

import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.autoconfigure.MobileCodeValidatorAutoConfiguration
import com.lhl.jwt.sample.service.UserAccountService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

/**
 * 短信验证码下发演示。
 *
 * jwt-starter 的默认 MobileCodeValidator 只负责「校验」，验证码由业务侧写入缓存，
 * key 固定为 sms:code:{mobile}，校验通过即消费（一次性）。
 *
 * 这里用固定验证码 123456 方便本地联调，生产环境应替换为真实短信中心，
 * 并且不要把验证码直接返回给前端。
 */
@RestController
@RequestMapping("/sms")
class SmsCodeController(
    private val userAccountService: UserAccountService
) {

    /** 向手机号下发验证码（示例固定 123456，5 分钟有效） */
    @PostMapping("/code/{mobile}")
    fun sendCode(@PathVariable mobile: String): ApiResult<Any> {
        if (userAccountService.loadByMobile(mobile) == null) {
            return ApiResult.fail(code = "404", msg = "手机号未绑定账号: $mobile")
        }
        MobileCodeValidatorAutoConfiguration.DefaultMobileCodeValidator.save(mobile, DEMO_CODE, CODE_TTL)
        return ApiResult.success(data = mapOf("mobile" to mobile, "code" to DEMO_CODE))
    }

    private companion object {
        const val DEMO_CODE = "123456"
        val CODE_TTL: Duration = Duration.ofMinutes(5)
    }
}
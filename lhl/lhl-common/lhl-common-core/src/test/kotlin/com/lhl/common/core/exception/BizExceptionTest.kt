package com.lhl.common.core.exception

import com.lhl.common.core.model.IBaseErrorCode
import io.swagger.v3.oas.annotations.tags.Tag
import org.junit.jupiter.api.Test

/**
 * @className: com.lhl.fw.common.exception.BizExceptionTest
 * @description: TODO 类描述
 * @author: 78772
 * @date:  2024/12/14 11:07
 **/
@Tag(name = "BizExceptionTest", description = "BizExceptionTest")
class BizExceptionTest {
    @Test
    fun `BizException of 带占位符参数`() {
        val userId = "12345"
        try {
            // 方式1：最常见、最推荐（带占位符参数）
            throw BizException.of(SubModuleError.USER_NOT_FOUND, "13800138000")
        } catch (ex: BizException) {
            println(ex.error)
        }
    }
    @Test
    fun `BizException of 不带参数使用默认msg`() {
        val userId = "12345"
        try {
            // 不带参数，直接用默认 msg
            throw BizException.of(SubModuleError.USER_DISABLED, userId)
        } catch (ex: BizException) {
            println(ex.message)
        }
    }

    @Test
    fun `BizException of 自定义消息`() {
        val userId = "12345"
        try {
            //完全自定义消息（覆盖枚举中的 msg）
            throw BizException.of("用户登录已超时，请重新登录")
        } catch (ex: BizException) {
            println(ex.message)
        }
    }
}

/**
 * 子模块专属错误码
 * 命名规范：模块缩写 + 五位错误码（强烈建议遵循阿里规范）
 * 示例：USER_ 表示用户模块
 */
enum class SubModuleError(override val code: String, override val msg: String) : IBaseErrorCode {
    // 用户相关（A0xxx 用户端错误）
    USER_NOT_FOUND("USER_0001", "用户[{0}]不存在"),
    USER_DISABLED("USER_0002", "用户[{0}]已被禁用"),
    USER_PASSWORD_ERROR("USER_0003", "密码错误，还剩 {0} 次机会"),

    // 订单相关（A1xxx）
    ORDER_NOT_FOUND("ORDER_0001", "订单[{0}]不存在"),
    ORDER_ALREADY_PAID("ORDER_0002", "订单[{0}]已支付，请勿重复操作"),
    ORDER_STOCK_INSUFFICIENT("ORDER_0003", "商品[{0}]库存不足，仅剩 {1} 件"),

    // 其他业务...
    ;
    companion object {
        // 方便通过 code 反查枚举（可选，但非常有用）
        private val codeMap = entries.associateBy { it.code }
        fun byCode(code: String): SubModuleError? = codeMap[code]
    }
}
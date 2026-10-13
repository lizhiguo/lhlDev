package com.lhl.common.core.exception

import com.lhl.common.core.model.CommonError
import com.lhl.common.core.model.IBaseErrorCode
import com.lhl.common.core.model.IBaseMsg

/**
 * 异常系统字段说明
 *
 * IBaseMsg 接口 - 基础消息接口,定义了 code(错误码/成功码) 和 msg(消息模板) 两个核心属性
 * IBaseErrorCode 接口 - 错误码特化接口,继承自 IBaseMsg,专门用于异常系统
 * IBaseSuccessMsg 接口 - 成功消息特化接口,继承自 IBaseMsg,用于成功响应
 *
 * BaseException 内部字段:
 * - formattedMessage: 最终呈现给前端/日志的消息（已替换占位符）
 * - customMessage: 手动覆盖的完整消息（最高优先级）
 *
 * 使用示例:
 * - BizException.of(DictErrorCode.DICT_NOT_FOUND, "dict001")
 * - BizException.of("用户登录已超时，请重新登录")
 */

/**
 *  异常基类
 *  所有自定义异常的基类，便于统一管理和扩展
 */
abstract class BaseException @JvmOverloads constructor(
    open val error: IBaseMsg,
    private val args: Array<out Any?> = emptyArray(),
    private val customMessage: String? = null,
    cause: Throwable? = null
) : RuntimeException(cause) {

    // 延迟计算消息，避免不必要格式化开销
    val formattedMessage: String by lazy {
        customMessage ?: error.format(*args)
    }

    val code: String get() = error.code

    // 用于统一异常响应体
    open val httpStatus: Int get() = when (this) {
        is BizException -> 400
        is AuthException -> 401   // 或 403，根据实际业务区分
        is ClientException -> 502
        is SysException -> 500
        else -> 500
    }

    override val message: String get() = formattedMessage

    // 便于日志打印完整信息
    override fun toString(): String {
        return "${this::class.simpleName}(code=$code, message='$formattedMessage')"
    }
}

/**
 * 业务异常基类
 * 状态码400
 * 表示业务逻辑相关的异常，例如：非法参数输入、商品库存不足等
 * 业务异常，例如：400和 404 错误
 * 项目编码+ 模块编码 + 错误编码：比如：PDM_USER_001或者更细的力度。同时需要维护一个错误码和错误描述对应的文档。
 * 阿里巴巴 Java 开发手册错误码：A0100 表示用户注册错误，错误码规则如下
 * - 五位数组成
 * - A 代表用户端错误
 * - B 代表当前系统执行异常
 * - C 代表第三方系统调用异常
 * - 错误类的步长预留 100
 *
 * @property errorCode
 * @property message
 */
class BizException private constructor(
    error: IBaseErrorCode,
    args: Array<out Any?>,
    customMessage: String?,
    cause: Throwable?
) : BaseException(error, args, customMessage, cause) {

    companion object {
        @JvmOverloads
        fun of(error: IBaseErrorCode, vararg args: Any?, cause: Throwable? = null) =
            BizException(error, args, null, cause)

        @JvmOverloads
        fun of(message: String, cause: Throwable? = null) =
            BizException(CommonError.SYSTEM_ERROR, emptyArray(), message, cause)
    }
}

/**
 * 系统异常
 * 状态码500
 */
class SysException private constructor(
    error: IBaseErrorCode,
    args: Array<out Any?>,
    customMessage: String?,
    cause: Throwable?
) : BaseException(error, args, customMessage, cause) {
    override val httpStatus: Int = 500

    companion object {
        @JvmOverloads
        fun of(error: IBaseErrorCode = CommonError.SYSTEM_ERROR, vararg args: Any?, cause: Throwable? = null) =
            SysException(error, args, null, cause)

        @JvmOverloads
        fun of(message: String, cause: Throwable? = null) =
            SysException(CommonError.SYSTEM_ERROR, emptyArray(), message, cause)
    }
}

/**
 * 表示外部系统调用失败异常，例如：调用第三方支付接口失败、微服务内接口调用报错等
 * 状态码502
 *
 */
class ClientException private constructor(
    error: IBaseErrorCode,
    args: Array<out Any?>,
    customMessage: String?,
    cause: Throwable?
) : BaseException(error, args, customMessage, cause) {
    override val httpStatus: Int = 502

    companion object {
        @JvmOverloads
        fun of(error: IBaseErrorCode = CommonError.THIRD_PARTY_ERROR, vararg args: Any?, cause: Throwable? = null) =
            ClientException(error, args, null, cause)
    }
}


/**
 * 表示认证和鉴权相关的异常，例如：Token 过期、无效 Token，用户无权访问资源等
 * 状态码401/403
 */
class AuthException private constructor(
    error: IBaseErrorCode,
    args: Array<out Any?>,
    customMessage: String?,
    cause: Throwable?
) : BaseException(error, args, customMessage, cause) {
    override val httpStatus: Int = 401 // 或 403

    companion object {
        @JvmOverloads
        fun of(error: IBaseErrorCode = CommonError.UNAUTHORIZED, vararg args: Any?, cause: Throwable? = null) =
            AuthException(error, args, null, cause)
    }
}

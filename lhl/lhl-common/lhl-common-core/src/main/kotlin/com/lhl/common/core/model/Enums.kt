package com.lhl.common.core.model

/**
 * 统一的成功枚举
 *
 * @property code
 * @property message
 */
enum class CommonSuccess(override val code: String, override val msg: String) : IBaseSuccessMsg {
    OK("200", "操作成功"),
    CREATED("201", "创建成功"),
    ACCEPTED("202", "请求已接受");
}
/**
 * 基础模块的通用错误码,命名规范为模块名_业务名
 * 以后在子模块中自己实现
 *
 * @property code
 * @property msg
 */
enum class CommonError(override val code: String, override val msg: String) : IBaseErrorCode {
    BAD_REQUEST("A0400", "请求参数错误"),
    UNAUTHORIZED("A0300", "未登录或登录已过期"),
    FORBIDDEN("A0310", "没有权限访问该资源"),
    NOT_FOUND("A0404", "资源不存在"),
    METHOD_NOT_ALLOWED("A0500", "不支持的请求方法"),
    TOO_MANY_REQUESTS("A0503", "请求过于频繁，请稍后再试"),
    SYSTEM_ERROR("B0001", "系统繁忙，请稍后再试"),
    SERVICE_UNAVAILABLE("B0002", "服务暂不可用"),
    GATEWAY_TIMEOUT("B0003", "网关超时"),
    THIRD_PARTY_ERROR("C0001", "第三方服务异常");

    companion object {
        private val codeMap = entries.associateBy { it.code }
        fun byCode(code: String): CommonError? = codeMap[code]
    }
}




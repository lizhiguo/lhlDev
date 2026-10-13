package com.lhl.common.core.model

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

@Schema(name = "统一响应消息报文")
data class ApiResult<Any>(
    @field:Schema(name = "是否成功", description = "true:成功,false:失败")
    val succ: Boolean? = true,

    @field:Schema(name = "状态code", description = "状态code")
    val code: String? = "200",

    @field:Schema(name = "服务器当前时间戳", description = "服务器当前时间戳")
    val ts: Long? = System.currentTimeMillis(),

    @field:Schema(name = "成功数据", description = "成功数据")
    val data: Any? = null,

    @field:Schema(name = "消息描述", description = "消息描述")
    val msg: String? = "请求成功",
) : Serializable {
    companion object {
        fun result(code: String?, msg: String?, data: Any? = null): ApiResult<Any> {
            return ApiResult(code = code, data = data, msg = msg)
        }

        fun success(
            code: String? = CommonSuccess.OK.code,
            msg: String? = CommonSuccess.OK.msg,
            data: Any? = null,
        ): ApiResult<Any> {
            return ApiResult(code = code, msg = msg, data = data)
        }

        fun successDef(
            code: String? = CommonSuccess.OK.code,
            msg: String? = CommonSuccess.OK.msg,
        ): ApiResult<Any> {
            return ApiResult(code = code, msg = msg)
        }

        fun fail(
            code: String? = CommonError.SYSTEM_ERROR.code,
            msg: String? = CommonError.SYSTEM_ERROR.msg,
            err: Map<String, String>? = null,
        ): ApiResult<Any> {
            return ApiResult(succ = false, code = code, msg = msg, data = err)
        }

        fun fail(
            code: String? = CommonError.SYSTEM_ERROR.code,
            msg: String? = CommonError.SYSTEM_ERROR.msg,
        ): ApiResult<Any> {
            return ApiResult(succ = false, code = code, msg = msg)
        }
    }
}



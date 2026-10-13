package com.lhl.demo.dict.adapter.`in`.rest

import com.lhl.common.core.exception.BizException
import com.lhl.common.core.model.ApiResult
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 数据字典模块统一异常处理器
 *
 * - 业务异常（BizException）映射为 400 + 对应错误码
 * - 参数校验失败（MethodArgumentNotValidException）映射为 400
 * - 其余异常映射为 500
 */
@RestControllerAdvice
class DictExceptionHandler {

    @ExceptionHandler(BizException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBiz(ex: BizException): ApiResult<Any> =
        ApiResult.fail(code = ex.code, msg = ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValid(ex: MethodArgumentNotValidException): ApiResult<Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "") }
        return ApiResult.fail(code = "400", msg = "参数校验失败", err = errors)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleOther(ex: Exception): ApiResult<Any> =
        ApiResult.fail(code = "500", msg = ex.message)
}

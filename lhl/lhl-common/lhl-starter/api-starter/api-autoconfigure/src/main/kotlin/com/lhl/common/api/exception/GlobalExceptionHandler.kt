package com.lhl.common.api.exception

import com.lhl.common.core.exception.BizException
import com.lhl.common.core.model.ApiResult
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MissingRequestValueException

@RestController
@ControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = KotlinLogging.logger {}
    }
    /**
     * IllegalArgumentException 使用断言Assert捕获异常信息
     */
    @ExceptionHandler(value = [IllegalArgumentException::class, IllegalStateException::class])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerIllegalArgumentException(ex: IllegalArgumentException): ApiResult<Any> {
        return ApiResult.fail(code = "500", msg = ex.message)
    }

    /**
     * 路径中没找到对应参数
     */
    @ExceptionHandler(value = [(MissingRequestValueException::class)])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerMissingRequestValueException(ex: MissingRequestValueException): ApiResult<Any> {
        return ApiResult.fail(code = "500", msg = "${ex.name}字段不存在")
    }

    @ExceptionHandler(value = [(MethodArgumentNotValidException::class)])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ApiResult<Any> {
        val bindingResult: BindingResult = ex.bindingResult
        val errors: List<FieldError> = bindingResult.fieldErrors
        val errorMessage = HashMap<String, String>(errors.size)
        for (error: FieldError in errors) {
            // 这里需要判空，因为defaultMessage不一定有返回值
            // 只有当有返回值的时候才执行后面的迭代操作
            error.defaultMessage?.let { errorMessage.put(error.field, it) }
        }
        // 这里将会使用上面定义好的数据了Result
        // 但是需要注意这里的code 400并不会改变返回给前端的status,只是用于状态判断
        return ApiResult(
            succ = false,
            msg = "请求参数错误",
            code = "400",
            ts = System.currentTimeMillis(),
            data = errorMessage
        )
    }

    @ExceptionHandler(value = [(WebExchangeBindException::class)])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBindException(bindException: WebExchangeBindException): ApiResult<Any> {
        val errors: List<FieldError> = bindException.bindingResult.fieldErrors
        val errorMessage = HashMap<String, String>(errors.size)
        for (error: FieldError in errors) {
            // 这里需要判空，因为defaultMessage不一定有返回值
            // 只有当有返回值的时候才执行后面的迭代操作
            error.defaultMessage?.let { errorMessage.put(error.field, it) }
        }
        // 这里将会使用上面定义好的数据了Result
        // 但是需要注意这里的code 400并不会改变返回给前端的status,只是用于状态判断
        return ApiResult(
            succ = false,
            msg = "请求参数错误",
            code = "400",
            ts = System.currentTimeMillis(),
            data = errorMessage
        )
    }

    @ExceptionHandler(value = [(BizException::class)])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleMethodBizException(ex: BizException): ApiResult<Any> {
        log.debug("------------全局捕获BIZ异常开始------------")
        ex.printStackTrace()
        // 这里将会使用上面定义好的数据了Result
        // 但是需要注意这里的code 400并不会改变返回给前端的status,只是用于状态判断
        return ApiResult(succ = false, code = "500", ts = System.currentTimeMillis(), msg = ex.message)
    }

    @ExceptionHandler(value = [(Exception::class)])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleMethodException(ex: Exception): ApiResult<Any> {
        log.debug("------------全局捕获未知异常开始------------")
        ex.printStackTrace()
        // 这里将会使用上面定义好的数据了Result
        // 但是需要注意这里的code 400并不会改变返回给前端的status,只是用于状态判断
        return ApiResult(succ = false, code = "500", msg = ex.message)
    }
}

/*
@ExceptionHandler(value = [(MethodArgumentNotValidException::class)])
@ResponseBody
fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): Response<HashMap<String, String>> {
    val bindingResult: BindingResult = ex.bindingResult
    val errors: List<FieldError> = bindingResult.fieldErrors
    val errorMessage = HashMap<String, String>(errors.size)
    for (error: FieldError in errors) {
        // 这里需要判空，因为defaultMessage不一定有返回值
        // 只有当有返回值的时候才执行后面的迭代操作
        error.defaultMessage?.let { errorMessage.put(error.field, it) };
    }
    // 这里将会使用上面定义好的数据了Result
    // 但是需要注意这里的code 404并不会改变返回给前端的status,只是用于状态判断
    return Response(404, err = errorMessage)
}*/

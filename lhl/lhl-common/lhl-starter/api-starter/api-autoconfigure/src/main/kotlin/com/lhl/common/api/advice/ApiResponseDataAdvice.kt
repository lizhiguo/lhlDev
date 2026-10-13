package com.lhl.common.api.advice

import cn.hutool.v7.json.JSONUtil
import com.lhl.common.api.annotation.IgnorReponseAdvice
import com.lhl.common.api.config.ApiFilterConfig
import com.lhl.common.core.model.ApiResult
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class ApiResponseDataAdvice(val apiFilterConfig: ApiFilterConfig) : ResponseBodyAdvice<Any> {
    companion object {
        private val log = KotlinLogging.logger {}
    }
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val uri = request.requestURI
        log.debug { "uri==$uri" }
        apiFilterConfig.adviceFilterStaticPath?.forEach {
            if (uri.startsWith(it)) {
                return false
            }
        }
        return filter(returnType)
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        log.debug { "beforeBodyWrite" }
        return when (body) {
            is Unit -> ApiResult.successDef()
            is String -> JSONUtil.toJsonStr(ApiResult.success(data = body))
            else -> ApiResult.success(data = body)
        }
    }

    private fun filter(methodParameter: MethodParameter): Boolean {
        //返回值类型是ApiResult,则不必加工
        if (methodParameter.parameterType == ApiResult::class.java) {
            return false
        }
        val declaringClass = methodParameter.declaringClass
        // 检查过滤包路径
        if(apiFilterConfig?.adviceFilterPackage?.contains(declaringClass.name) == true){
            return false

        }
        // 检查过滤类路径
        if(apiFilterConfig?.adviceFilterClass?.contains(declaringClass.name) == true){
            return false
        }
        // 检查注解是否存在
        if(declaringClass.isAnnotationPresent(IgnorReponseAdvice::class.java)
            || methodParameter.hasMethodAnnotation(IgnorReponseAdvice::class.java)){
            return false
        }
        return true
    }
}
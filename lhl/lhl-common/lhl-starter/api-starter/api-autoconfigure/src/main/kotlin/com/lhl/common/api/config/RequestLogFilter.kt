package com.lhl.common.api.config

import cn.hutool.v7.core.text.StrUtil
import com.lhl.common.core.helper.parseLocalDateTime
import com.lhl.common.core.helper.toPrettyJsonString
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.util.Arrays
import java.util.Collections
import java.util.UUID

@Component
class RequestLogFilter : GenericFilterBean() {

    companion object {
        private const val REQUEST_ID = "tracId"
        /** 缓存请求/响应体的最大字节数，8KB 足够覆盖绝大多数 API 请求体 */
        private const val CACHE_LIMIT = 8192
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val requestWrapper = ContentCachingRequestWrapper(request as HttpServletRequest, CACHE_LIMIT)
        val responseWrapper = ContentCachingResponseWrapper(response as HttpServletResponse)

        // 尝试从请求中获取或生成一个唯一的请求ID
        val requestId = requestWrapper.getHeader(REQUEST_ID) ?: UUID.randomUUID().toString()

        try {
            // 将请求ID添加到MDC中，以便可以在日志中使用它
            MDC.put(REQUEST_ID, requestId)
            val startTime = System.currentTimeMillis()
            logger.info("请求开始时间:${startTime.parseLocalDateTime()}")
            chain.doFilter(requestWrapper, responseWrapper)

            logRequest(requestWrapper)
            logResponse(responseWrapper)
            val endTime = System.currentTimeMillis()
            logger.info("请求结束时间:${startTime.parseLocalDateTime()},请求耗时:${endTime-startTime}")
        } finally {
            // 确保MDC被清理，避免内存泄漏
            MDC.clear()
        }
    }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val parameters = parametersToString(request.parameterMap)
        val headers = headersToString(Collections.list(request.headerNames)) { request.getHeader(it) }
        val body = String(request.contentAsByteArray)
        val logMap = linkedMapOf(
            "Parameters:" to parameters,
            "Headers:" to headers,
            "Body:" to body
        )
        val logString = joinMapIntoString(logMap)
        logger.info("请求:${logMap.toPrettyJsonString()}")
        logger.info("请求:API: 方法:${request.method} 路径:${request.requestURI} 参数:$logString")
    }

    @Throws(IOException::class)
    private fun logResponse(response: ContentCachingResponseWrapper) {
        val headers = headersToString(response.headerNames) { response.getHeader(it) }
        val body = String(response.contentAsByteArray)
        val logMap = linkedMapOf(
            "Status" to response.status.toString(),
            "Headers:" to headers,
            "Body:" to body
        )
//        val logString = joinMapIntoString(logMap)
        logger.info("响应: ${logMap.toPrettyJsonString()}")
        response.copyBodyToResponse()
    }

    private fun joinMapIntoString(logMap: Map<String, String>): String {
        return logMap.filter { StrUtil.isNotBlank(it.value) }
            .map { it.key + " " + it.value }
            .joinToString("")
    }

    private fun headersToString(headerNames: Collection<String>, headerValueResolver: (String) -> String?): String {
        return headerNames.joinToString(",") { header ->
            "$header=${headerValueResolver(header)}"
        }
    }

    private fun parametersToString(parameterMap: Map<String, Array<String>>): String {
        return parameterMap.entries.joinToString(",") { (key, value) ->
            "$key=${Arrays.toString(value)}"
        }
    }
}
package com.lhl.common.core.config

import cn.hutool.v7.extra.spring.SpringUtil
import com.lhl.common.core.helper.JacksonHelper
import com.lhl.common.core.helper.toJsonString
import io.github.oshai.kotlinlogging.KotlinLogging


import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import tools.jackson.databind.ObjectMapper
import java.lang.reflect.Method
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/**
 * @className: com.lhl.fw.core.config.Config
 * @description: TODO 类描述
 * @author: 78772
 * @date:  2024/11/28 20:40
 **/
private val log = KotlinLogging.logger {}

@Import(SpringUtil::class)
class SpringBeanConfig

@Configuration
@EnableAsync
class SpringAsyncConfig : AsyncConfigurer {
    override fun getAsyncExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            // 核心线程数 corePoolSize = CPU线程数/2 Runtime.getRuntime().availableProcessors()//获取逻辑核心数，如6核心12线程，那么返回的是12
            corePoolSize = 10
            // 最大线程数 maxPoolSize = CPU 线程数
            maxPoolSize = 50
            // 队列最大长度 >=mainExecutor.maxSize  = ( corePoolSize / 任务处理时间 ) * 期望等待时间(秒)
            queueCapacity = 1000
            // 线程前缀名
            setThreadNamePrefix("SpringAsync-")
            // 线程池维护线程所允许的空闲时间
            keepAliveSeconds = 300
            setAwaitTerminationSeconds(60)
            setWaitForTasksToCompleteOnShutdown(true)
            // 修改拒绝策略为使用当前线程执行，如果线程池满了, 丢给caller线程执行
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            // 初始化线程
            initialize()
        }
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return CustomAsyncExceptionHandler()
    }
}

/**
 * 异步操作异常
 */
class CustomAsyncExceptionHandler : AsyncUncaughtExceptionHandler {
    override fun handleUncaughtException(
        ex: Throwable,
        method: Method,
        vararg params: Any?,
    ) {
        params.forEach {
            error { "${"........SpringAsyncExceptionHandler=={}"} ${it?.toJsonString()}" }
        }
    }
}

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper = JacksonHelper.mapper
}
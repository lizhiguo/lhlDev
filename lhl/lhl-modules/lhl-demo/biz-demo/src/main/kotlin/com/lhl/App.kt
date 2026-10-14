package com.lhl

import cn.hutool.v7.json.JSONUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class App

fun main(args: Array<String>) {
    val context = runApplication<App>(*args)
    println("bean总数:${context.beanDefinitionCount}")
    println("bean名称:${JSONUtil.toJsonPrettyStr(context.beanDefinitionNames)}")
}
package com.lhl.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * 数据字典模块启动类（位于 ddd-demo-ms）
 *
 * 依赖 ddd-demo-biz 提供的领域/应用/适配器分层代码（包 [com.lhl.demo.dict]）。
 * 使用 H2 内存库，开箱即跑，无需外部数据库。
 */
@SpringBootApplication(scanBasePackages = ["com.lhl.demo.dict"])
@EnableJpaAuditing
class DictApplication

fun main(args: Array<String>) {
    runApplication<DictApplication>(*args)
}

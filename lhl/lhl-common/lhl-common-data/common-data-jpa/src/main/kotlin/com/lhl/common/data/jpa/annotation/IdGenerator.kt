package com.lhl.common.data.jpa.annotation


import com.lhl.common.data.jpa.helper.SnowflakeGenerator
import org.hibernate.annotations.IdGeneratorType

/**
 * @className: com.lhl.fw.rdb.annotations.IdGenerator
 * @description: TODO 类描述
 * @author: 78772
 * @date:  2024/11/30 20:17
 **/
@IdGeneratorType(SnowflakeGenerator::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IdGenerator

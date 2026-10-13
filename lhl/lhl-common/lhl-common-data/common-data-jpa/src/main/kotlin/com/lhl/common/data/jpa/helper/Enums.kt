package com.lhl.common.data.jpa.helper

import io.swagger.v3.oas.annotations.tags.Tag

/**
 * @className: com.lhl.fw.rdb.helpers.BaseDbEnum
 * @description: 枚举基类接口,枚举类需要实现此接口以支持数据库值映射
 * @author: king
 * @date: 2020-12-10 8:33
 **/
@Tag(name = "枚举通用接口", description = "枚举通用数据库接口")
interface BaseDbEnum<E> {
    fun getDbValue(): E
}
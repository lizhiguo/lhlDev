package com.lhl.common.data.mybatis.helper

import com.lhl.common.core.model.PageResult
import com.github.pagehelper.Page

/**
 * 将 MyBatis PageHelper 的 Page 对象转换为统一的 PageResult 分页结果
 *
 * @param T 数据类型
 * @return 包含分页信息的 PageResult 对象
 */
fun <T> Page<T>.toPageResult(): PageResult<T> {
    return PageResult(
        total = this.total,
        pages = this.pages,
        current = this.pageNum,
        size = this.pageSize,
        items = this.result
    )
}

/**
 * 将 MyBatis PageHelper 的 Page 对象转换为统一的 PageResult 分页结果，并对数据进行转换
 *
 * @param T 原始数据类型
 * @param R 转换后的数据类型
 * @param transform 数据转换函数，将类型 T 转换为类型 R
 * @return 包含分页信息和转换后数据的 PageResult 对象
 */
fun <T, R> Page<T>.toPageResult(transform: (T) -> R): PageResult<R> {
    return PageResult(
        total = this.total,
        pages = this.pages,
        current = this.pageNum,
        size = this.pageSize,
        items = this.result.map(transform)
    )
}

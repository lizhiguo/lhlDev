package com.lhl.demo.dict.app.query

import com.lhl.demo.dict.api.model.DictItemCond

/**
 * 字典项查询对象（CQRS Query）
 */
sealed class DictItemQuery {
    data class GetQuery(val entityId: Long) : DictItemQuery()
    data class ListQuery(val cond: DictItemCond) : DictItemQuery()
    data class PageQuery(val cond: DictItemCond, val pageNum: Int, val pageSize: Int) : DictItemQuery()
}

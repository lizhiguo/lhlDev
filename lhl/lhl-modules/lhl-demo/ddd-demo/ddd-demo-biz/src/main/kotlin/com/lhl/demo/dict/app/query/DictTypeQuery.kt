package com.lhl.demo.dict.app.query

import com.lhl.demo.dict.api.model.DictTypeCond

/**
 * 字典类型查询对象（CQRS Query）
 */
sealed class DictTypeQuery {
    data class GetQuery(val entityId: Long) : DictTypeQuery()
    data class ListQuery(val cond: DictTypeCond) : DictTypeQuery()
    data class PageQuery(val cond: DictTypeCond, val pageNum: Int, val pageSize: Int) : DictTypeQuery()
}

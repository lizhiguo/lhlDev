package com.lhl.demo.dict.app.command

import com.lhl.demo.dict.app.port.`in`.DictItemQueryCase
import com.lhl.demo.dict.app.port.out.DictItemDao
import com.lhl.demo.dict.app.query.DictItemQuery
import org.jmolecules.architecture.hexagonal.PrimaryAdapter
import org.springframework.stereotype.Component

/**
 * 字典项查询处理器（读操作入站适配器）
 */
@PrimaryAdapter
@Component
class DictItemQueryHandler(private val dao: DictItemDao) : DictItemQueryCase {
    override fun handle(query: DictItemQuery.GetQuery) = dao.findById(query.entityId)
    override fun handle(query: DictItemQuery.ListQuery) = dao.findListByCond(query.cond)
    override fun handle(query: DictItemQuery.PageQuery) =
        dao.findPageByCond(query.cond, query.pageNum, query.pageSize)
}

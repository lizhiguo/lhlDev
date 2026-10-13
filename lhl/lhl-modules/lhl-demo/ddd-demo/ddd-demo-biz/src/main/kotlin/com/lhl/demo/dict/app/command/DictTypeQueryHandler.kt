package com.lhl.demo.dict.app.command

import com.lhl.demo.dict.app.port.`in`.DictTypeQueryCase
import com.lhl.demo.dict.app.port.out.DictTypeDao
import com.lhl.demo.dict.app.query.DictTypeQuery
import org.jmolecules.architecture.hexagonal.PrimaryAdapter
import org.springframework.stereotype.Component

/**
 * 字典类型查询处理器（读操作入站适配器）
 *
 * 只读操作，直接转发到 Dao，不注入 Repository。
 */
@PrimaryAdapter
@Component
class DictTypeQueryHandler(private val dao: DictTypeDao) : DictTypeQueryCase {
    override fun handle(query: DictTypeQuery.GetQuery) = dao.findById(query.entityId)
    override fun handle(query: DictTypeQuery.ListQuery) = dao.findListByCond(query.cond)
    override fun handle(query: DictTypeQuery.PageQuery) =
        dao.findPageByCond(query.cond, query.pageNum, query.pageSize)
}

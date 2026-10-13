package com.lhl.demo.dict.app.port.`in`

import com.lhl.common.core.model.PageResult
import com.lhl.demo.dict.api.model.DictItemView
import com.lhl.demo.dict.app.query.DictItemQuery
import org.jmolecules.architecture.hexagonal.PrimaryPort

/**
 * 字典项读操作入站端口
 */
@PrimaryPort
interface DictItemQueryCase {
    fun handle(query: DictItemQuery.GetQuery): DictItemView?
    fun handle(query: DictItemQuery.ListQuery): List<DictItemView>?
    fun handle(query: DictItemQuery.PageQuery): PageResult<DictItemView>?
}

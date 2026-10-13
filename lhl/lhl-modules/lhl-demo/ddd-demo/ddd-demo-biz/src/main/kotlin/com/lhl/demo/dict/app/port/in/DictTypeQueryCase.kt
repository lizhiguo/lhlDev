package com.lhl.demo.dict.app.port.`in`

import com.lhl.common.core.model.PageResult
import com.lhl.demo.dict.api.model.DictTypeView
import com.lhl.demo.dict.app.query.DictTypeQuery
import org.jmolecules.architecture.hexagonal.PrimaryPort

/**
 * 字典类型读操作入站端口
 */
@PrimaryPort
interface DictTypeQueryCase {
    fun handle(query: DictTypeQuery.GetQuery): DictTypeView?
    fun handle(query: DictTypeQuery.ListQuery): List<DictTypeView>?
    fun handle(query: DictTypeQuery.PageQuery): PageResult<DictTypeView>?
}

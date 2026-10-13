package com.lhl.demo.dict.app.helper

import com.lhl.demo.dict.api.model.DictItemDTO
import com.lhl.demo.dict.api.model.DictTypeDTO
import com.lhl.demo.dict.domain.model.DictItem
import com.lhl.demo.dict.domain.model.DictType

/**
 * 应用层装配器：领域实体 → 写响应 DTO
 *
 * 使用 Kotlin 扩展函数（不使用 MapStruct）；枚举展开为 code + 描述；
 * 审计字段通过 [also] 赋值。
 */

/** 字典类型 → DTO */
fun DictType.toDto(): DictTypeDTO = DictTypeDTO(
    id = id!!.value,
    dictCode = dictCode,
    name = name,
    description = description,
    status = status?.code,
    statusDesc = status?.description,
).also {
    it.createTime = createTime?.toString()
    it.updateTime = updateTime?.toString()
}

/** 字典项 → DTO */
fun DictItem.toDto(): DictItemDTO = DictItemDTO(
    id = id!!.value,
    dictTypeCode = dictTypeCode,
    itemCode = itemCode,
    itemName = itemName,
    sort = sort,
    status = status?.code,
    statusDesc = status?.description,
    remark = remark,
).also {
    it.createTime = createTime?.toString()
    it.updateTime = updateTime?.toString()
}

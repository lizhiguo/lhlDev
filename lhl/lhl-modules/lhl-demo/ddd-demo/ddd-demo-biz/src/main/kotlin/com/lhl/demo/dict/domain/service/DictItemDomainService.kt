package com.lhl.demo.dict.domain.service

import com.lhl.common.core.exception.BizException
import com.lhl.common.data.jpa.helper.*
import com.lhl.demo.dict.domain.exception.DictItemErrorCode
import com.lhl.demo.dict.domain.model.DictItem
import com.lhl.demo.dict.domain.repo.DictItemRepository
import org.springframework.stereotype.Service

/**
 * 字典项领域服务
 *
 * 职责：按 ID 查询聚合根 + 业务规则校验（同类型下字典项编码唯一性）+ 返回领域数据。
 * 不调用任何持久化写操作、不调用 Dao / Mapper。
 */
@Service
class DictItemDomainService(private val repository: DictItemRepository) {

    /** 按 ID 获取字典项，不存在则抛业务异常 */
    fun getDictItem(id: DictItem.DictItemId): DictItem =
        repository.findByIdOrNull(id)
            ?: throw BizException.of(DictItemErrorCode.DICT_ITEM_NOT_FOUND, id)

    /** 唯一性校验：同一字典类型下，字典项编码不能重复（排除自身） */
    fun validateItemCodeUniqueness(dictTypeCode: String?, itemCode: String?, excludeId: DictItem.DictItemId? = null) {
        if (dictTypeCode == null || itemCode == null) return
        if (repository.exists(DictItemSpec.uniqueCheck(dictTypeCode, itemCode, excludeId))) {
            throw BizException.of(DictItemErrorCode.DICT_ITEM_CODE_DUPLICATE, itemCode)
        }
    }
}

/** 字典项查询条件规范（强制集中管理） */
object DictItemSpec {
    fun uniqueCheck(dictTypeCode: String?, itemCode: String?, excludeId: DictItem.DictItemId?): Specification<DictItem> {
        val byType = dictTypeCode?.let { DictItem::dictTypeCode.equal(it) }
        val byCode = itemCode?.let { DictItem::itemCode.equal(it) }
        val excludeSelf = excludeId?.let { DictItem::id.notEqual(it) }
        return and(excludeSelf, byType, byCode)
    }

    fun byTypeCode(dictTypeCode: String): Specification<DictItem> =
        DictItem::dictTypeCode.equal(dictTypeCode)
}

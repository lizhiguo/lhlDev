package com.lhl.demo.dict.domain.service

import com.lhl.common.core.exception.BizException
import com.lhl.common.data.jpa.helper.*
import com.lhl.demo.dict.domain.exception.DictTypeErrorCode
import com.lhl.demo.dict.domain.model.DictType
import com.lhl.demo.dict.domain.model.DictTypeStatus
import com.lhl.demo.dict.domain.repo.DictTypeRepository
import org.springframework.stereotype.Service

/**
 * 字典类型领域服务
 *
 * 职责（仅三件事）：
 * 1. 按 ID 查询聚合根
 * 2. 业务规则校验（编码唯一性）
 * 3. 返回领域数据给 CommandHandler
 *
 * 不调用任何持久化写操作、不调用 Dao / Mapper。
 */
@Service
class DictTypeDomainService(private val repository: DictTypeRepository) {

    /** 按 ID 获取字典类型，不存在则抛业务异常 */
    fun getDictType(id: DictType.DictTypeId): DictType =
        repository.findByIdOrNull(id)
            ?: throw BizException.of(DictTypeErrorCode.DICT_TYPE_NOT_FOUND, id)

    /** 唯一性校验：字典类型编码不能重复（排除自身） */
    fun validateDictCodeUniqueness(dictCode: String?, excludeId: DictType.DictTypeId? = null) {
        if (dictCode == null) return
        if (repository.exists(DictTypeSpec.uniqueCheck(dictCode, excludeId))) {
            throw BizException.of(DictTypeErrorCode.DICT_TYPE_CODE_DUPLICATE, dictCode)
        }
    }
}

/** 字典类型查询条件规范（强制集中管理，禁止在方法内拼 Specification） */
object DictTypeSpec {
    fun uniqueCheck(dictCode: String?, excludeId: DictType.DictTypeId?): Specification<DictType> {
        val byCode = dictCode?.let { DictType::dictCode.equal(it) }
        val excludeSelf = excludeId?.let { DictType::id.notEqual(it) }
        return and(excludeSelf, byCode)
    }

    fun byStatus(status: String): Specification<DictType> =
        DictType::status.equal(DictTypeStatus.fromCode(status))
}

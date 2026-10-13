package com.lhl.demo.dict.app.command

import com.lhl.common.core.exception.BizException
import com.lhl.demo.dict.app.helper.toDto
import com.lhl.demo.dict.app.port.`in`.DictTypeUseCase
import com.lhl.demo.dict.domain.exception.DictTypeErrorCode
import com.lhl.demo.dict.domain.model.DictType
import com.lhl.demo.dict.domain.model.DictTypeStatus
import com.lhl.demo.dict.domain.repo.DictItemRepository
import com.lhl.demo.dict.domain.repo.DictTypeRepository
import com.lhl.demo.dict.domain.service.DictItemSpec
import com.lhl.demo.dict.domain.service.DictTypeDomainService
import org.jmolecules.architecture.hexagonal.PrimaryAdapter
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 字典类型命令处理器（写操作入站适配器）
 *
 * 编排：领域校验 → 命令转 Attr → 聚合根创建/修改 → 仓储保存。
 */
@PrimaryAdapter
@Component
@Transactional(rollbackFor = [Exception::class])
class DictTypeCommandHandler(
    private val repository: DictTypeRepository,
    private val domainService: DictTypeDomainService,
    private val itemRepository: DictItemRepository,
) : DictTypeUseCase {

    override fun handle(cmd: DictTypeCmd.Create): com.lhl.demo.dict.api.model.DictTypeDTO {
        domainService.validateDictCodeUniqueness(cmd.dictCode, null)
        val entity = DictType.create(cmd.toAttr())
        repository.save(entity)
        return entity.toDto()
    }

    override fun handle(cmd: DictTypeCmd.Modify): com.lhl.demo.dict.api.model.DictTypeDTO {
        val entityId = DictType.DictTypeId(cmd.entityId)
        domainService.validateDictCodeUniqueness(cmd.dictCode, entityId)
        val entity = domainService.getDictType(entityId)
        entity.modify(cmd.toAttr())
        repository.save(entity)
        return entity.toDto()
    }

    override fun handle(cmd: DictTypeCmd.Delete) {
        val entity = domainService.getDictType(DictType.DictTypeId(cmd.entityId))
        // 跨聚合保护：类型下仍存在字典项时禁止删除
        if (itemRepository.exists(DictItemSpec.byTypeCode(entity.dictCode ?: ""))) {
            throw BizException.of(DictTypeErrorCode.DICT_TYPE_HAS_ITEMS, entity.dictCode)
        }
        repository.delete(entity)
    }

    override fun handle(cmd: DictTypeCmd.ChangeStatus): com.lhl.demo.dict.api.model.DictTypeDTO {
        val entity = domainService.getDictType(DictType.DictTypeId(cmd.entityId))
        entity.changeStatus(DictTypeStatus.fromCode(cmd.status))
        repository.save(entity)
        return entity.toDto()
    }
}

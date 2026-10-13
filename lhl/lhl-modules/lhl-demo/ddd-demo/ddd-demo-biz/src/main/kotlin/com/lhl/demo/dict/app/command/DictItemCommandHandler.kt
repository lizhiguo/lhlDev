package com.lhl.demo.dict.app.command

import com.lhl.demo.dict.app.helper.toDto
import com.lhl.demo.dict.app.port.`in`.DictItemUseCase
import com.lhl.demo.dict.domain.model.DictItem
import com.lhl.demo.dict.domain.model.DictItemStatus
import com.lhl.demo.dict.domain.repo.DictItemRepository
import com.lhl.demo.dict.domain.service.DictItemDomainService
import org.jmolecules.architecture.hexagonal.PrimaryAdapter
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 字典项命令处理器（写操作入站适配器）
 */
@PrimaryAdapter
@Component
@Transactional(rollbackFor = [Exception::class])
class DictItemCommandHandler(
    private val repository: DictItemRepository,
    private val domainService: DictItemDomainService,
) : DictItemUseCase {

    override fun handle(cmd: DictItemCmd.Create): com.lhl.demo.dict.api.model.DictItemDTO {
        domainService.validateItemCodeUniqueness(cmd.dictTypeCode, cmd.itemCode, null)
        val entity = DictItem.create(cmd.toAttr())
        repository.save(entity)
        return entity.toDto()
    }

    override fun handle(cmd: DictItemCmd.Modify): com.lhl.demo.dict.api.model.DictItemDTO {
        val entityId = DictItem.DictItemId(cmd.entityId)
        val entity = domainService.getDictItem(entityId)
        // 同类型下字典项编码唯一性校验（若修改了编码）
        domainService.validateItemCodeUniqueness(entity.dictTypeCode, cmd.itemCode, entityId)
        entity.modify(cmd.toAttr())
        repository.save(entity)
        return entity.toDto()
    }

    override fun handle(cmd: DictItemCmd.Delete) {
        val entity = domainService.getDictItem(DictItem.DictItemId(cmd.entityId))
        repository.delete(entity)
    }

    override fun handle(cmd: DictItemCmd.ChangeStatus): com.lhl.demo.dict.api.model.DictItemDTO {
        val entity = domainService.getDictItem(DictItem.DictItemId(cmd.entityId))
        entity.changeStatus(DictItemStatus.fromCode(cmd.status))
        repository.save(entity)
        return entity.toDto()
    }
}

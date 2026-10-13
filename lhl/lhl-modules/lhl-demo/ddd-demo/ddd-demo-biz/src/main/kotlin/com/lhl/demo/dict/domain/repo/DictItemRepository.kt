package com.lhl.demo.dict.domain.repo

import com.lhl.common.data.ddd.jpa.domain.repo.IBaseDomainRepository
import com.lhl.demo.dict.domain.model.DictItem
import org.jmolecules.architecture.hexagonal.SecondaryPort
import org.jmolecules.ddd.annotation.Repository
import org.springframework.data.jpa.domain.Specification

/**
 * 字典项领域仓储（出站端口）
 */
@Repository
@SecondaryPort
interface DictItemRepository : IBaseDomainRepository<DictItem, DictItem.DictItemId> {
    /** 存在性校验，供领域服务做唯一性检查 */
    fun exists(spec: Specification<DictItem>): Boolean
}

package com.lhl.demo.dict.domain.repo

import com.lhl.common.data.ddd.jpa.domain.repo.IBaseDomainRepository
import com.lhl.demo.dict.domain.model.DictType
import org.jmolecules.architecture.hexagonal.SecondaryPort
import org.jmolecules.ddd.annotation.Repository
import org.springframework.data.jpa.domain.Specification

/**
 * 字典类型领域仓储（出站端口）
 *
 * 仅定义领域需要的特殊方法；普通读写方法由 [com.lhl.demo.dict.adapter.out.persistence.jpa.DictTypeJpaAdapter] 提供。
 */
@Repository
@SecondaryPort
interface DictTypeRepository : IBaseDomainRepository<DictType, DictType.DictTypeId> {
    /** 存在性校验，供领域服务做唯一性检查 */
    fun exists(spec: Specification<DictType>): Boolean
}

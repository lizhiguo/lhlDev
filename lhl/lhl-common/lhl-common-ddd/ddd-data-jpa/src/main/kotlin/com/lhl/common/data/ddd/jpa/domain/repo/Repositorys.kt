package com.lhl.common.data.ddd.jpa.domain.repo


import com.lhl.common.data.jpa.model.entity.JpaEntity
import com.lhl.common.data.jpa.repository.IBaseJpaRepository
import org.jmolecules.ddd.types.Identifier
import org.springframework.data.repository.NoRepositoryBean

/**
 * Domain Repository
 */
@NoRepositoryBean
interface  IDddBaseJpaRepository<E : JpaEntity<ID>, ID : Identifier> : IBaseJpaRepository<E, ID>

/**
 * 领域仓库
 */
interface IBaseDomainRepository<E,ID> {
    fun save(entity: E): E
    fun delete(entity: E)
    fun findByIdOrNull(id: ID): E?
    fun existsById(id: ID): Boolean
    fun findHistoryByIdOrNull(id: ID): List<E?>
}
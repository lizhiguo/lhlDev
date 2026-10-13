package com.lhl.common.data.ddd.jpa.adapter


import com.lhl.common.data.jpa.model.entity.JpaEntity
import com.lhl.common.data.ddd.jpa.domain.repo.IBaseDomainRepository
import com.lhl.common.data.ddd.jpa.domain.repo.IDddBaseJpaRepository
import org.jmolecules.ddd.types.Identifier

import org.springframework.data.repository.findByIdOrNull

abstract class BaseJpaAdapter<E : JpaEntity<ID>, ID : Identifier, R : IDddBaseJpaRepository<E, ID>>(
    protected val repository: R
) : IBaseDomainRepository<E, ID> {

    override fun save(entity: E): E = repository.save(entity)

    override fun delete(entity: E) = repository.delete(entity)

    override fun findByIdOrNull(id: ID): E? = repository.findByIdOrNull(id)

    override fun existsById(id: ID): Boolean = repository.existsById(id)

    override fun findHistoryByIdOrNull(id: ID): List<E?> = emptyList()
}
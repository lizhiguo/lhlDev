package com.lhl.common.data.jpa.repository


import com.lhl.common.data.jpa.model.entity.JpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.history.RevisionRepository

@NoRepositoryBean
interface IBaseJpaRepository<E, ID> : JpaRepository<E, ID>,
    JpaSpecificationExecutor<E>,
    RevisionRepository<E, ID, Long> where E : JpaEntity<ID>, ID : Any


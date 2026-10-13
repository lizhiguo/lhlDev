package com.lhl.common.data.ddd.jpa.domain.entity

import com.lhl.common.data.jpa.model.entity.AbstractAuditableJpaEntity
import com.lhl.common.data.jpa.model.entity.AbstractAuditableJpaTreeEntity
import com.lhl.common.data.jpa.model.entity.BaseEntity
import com.lhl.common.data.jpa.model.entity.BaseTreeEntity
import com.lhl.common.data.jpa.model.entity.JpaEntity
import com.lhl.common.data.jpa.model.entity.JpaTreeEntity

typealias DddJpaEntity<ID> = JpaEntity<ID>
typealias DddBaseEntity<E, ID> = BaseEntity<E, ID>
typealias DddAbstractAuditableJpaEntity<E, ID> = AbstractAuditableJpaEntity<E, ID>
typealias DddBaseTreeEntity<E, ID> = BaseTreeEntity<E, ID>
typealias DddJpaTreeEntity<T, ID> = JpaTreeEntity<T, ID>
typealias DddAbstractAuditableJpaTreeEntity<E, ID> = AbstractAuditableJpaTreeEntity<E, ID>
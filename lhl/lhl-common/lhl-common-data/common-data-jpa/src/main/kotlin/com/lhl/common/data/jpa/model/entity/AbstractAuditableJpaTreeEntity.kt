package com.lhl.common.data.jpa.model.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 抽象审计树型实体类
 * - 继承 AbstractAuditableJpaEntity 类
 * - 添加父级实体引用和子级实体集合属性
 * - 添加 @MappedSuperclass 注解，表示该类为抽象类，不能被实例化
 * - 添加 @EntityListeners(AuditingEntityListener::class) 注解，表示该类需要使用审计功能
 * */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractAuditableJpaTreeEntity<E, ID> :
    AbstractAuditableJpaEntity<E, ID>(),
    JpaTreeEntity<E, ID>
        where E : AbstractAuditableJpaTreeEntity<E, ID> {

    /**
     * 父节点（懒加载，避免 N+1）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    override var parent: E? = null

    /**
     * 子节点集合（级联操作）
     */
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    override val children: MutableSet<E> = mutableSetOf<E>()

}
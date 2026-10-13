package com.lhl.common.data.jpa.model.entity

import jakarta.persistence.*

@MappedSuperclass
abstract class BaseTreeEntity<E, ID> :
    BaseEntity<E, ID>(), JpaTreeEntity<E, ID> where E : BaseTreeEntity<E, ID> {

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

    // 继承 addChildren/removeChildren，无需重写
}
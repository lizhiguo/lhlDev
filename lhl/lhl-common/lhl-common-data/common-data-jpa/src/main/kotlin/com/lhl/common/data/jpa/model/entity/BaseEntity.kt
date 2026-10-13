package com.lhl.common.data.jpa.model.entity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import tools.jackson.databind.node.ObjectNode

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity<E, ID> :
    AbstractAggregateRoot<E>(),
    JpaEntity<ID> where E : BaseEntity<E, ID> {


    /**
     * 版本（乐观锁）
     */
    @Version
    @Column(name = "version", nullable = false, comment = "版本,乐观锁")
    @Schema(description = "版本", hidden = true)
    @JdbcTypeCode(SqlTypes.BIGINT)
    var version: Long? = 0L

    /**
     * 扩展 JSON 字段（DDD 领域扩展）
     */
    @Column(name = "ext_json_object", comment = "扩展字段")
    @JdbcTypeCode(SqlTypes.JSON)
    var extJsonObject: ObjectNode? = null

    /**
     * 判断实体是否为新实体
     *
     * 通过检查ID是否为null来判断实体是否为新创建的实体。
     * 如果ID为null，则认为是新实体；否则认为是已存在的实体。
     *
     * @return true表示是新实体，false表示已存在的实体
     */
    @get:Transient
    override val isNew: Boolean get() = id == null

    /**
     * 重写equals方法
     *
     * 基于实体类型和ID进行相等性比较。如果两个实体都是同一类型且ID相同，则认为它们相等。
     *
     * @param other 要比较的对象
     * @return 如果对象相等返回true，否则返回false
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BaseEntity<*, *>
        return id == other.id
    }
    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "${this::class.simpleName}(id=$id)"

}
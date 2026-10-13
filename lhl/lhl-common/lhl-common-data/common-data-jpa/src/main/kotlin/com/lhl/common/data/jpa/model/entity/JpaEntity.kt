package com.lhl.common.data.jpa.model.entity


/**
 * JPA实体接口，继承自Spring Data的Persistable接口
 * @param ID 实体主键类型
 */
interface JpaEntity<ID>: PersistenceObject {
    /**
     * 实体主键字段
     */
    var id: ID?

    val identifier: ID get() = id!!
}

package com.lhl.common.data.jpa.model.entity


/**
 * JPA树形实体接口
 *
 * 用于标识和标记具有树形结构特征的JPA实体。实现此接口的实体通常具有父子关系，
 * 可以构建层次化的数据结构，如组织架构、分类目录等。
 *
 * 此接口目前为空，作为标记接口使用，具体的树形结构实现应在实现类中定义。
 */
interface JpaTreeEntity<T, ID> : JpaEntity<ID> where T: JpaTreeEntity<T, ID>{

    var parent: T?

    val children: MutableSet<T>

    /**
     * 添加子节点
     * @param childs 子节点
     * @return void
     */
    @Suppress("UNCHECKED_CAST")
    fun addChildren(vararg childs: T) {
        childs.forEach {
            if (children.add(it)) {
                it.parent = this as T
            }
        }
    }

    /**
     * 移除子节点
     * @param childs 子节点
     * @return void
     */
    fun removeChildren(vararg childs: T) {
        childs.forEach {
            if (children.remove(it)) {
                it.parent = null
            }
        }
    }
}

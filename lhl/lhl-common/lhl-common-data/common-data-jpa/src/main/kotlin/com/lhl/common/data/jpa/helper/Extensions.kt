package com.lhl.common.data.jpa.helper


import cn.hutool.v7.extra.spring.SpringUtil
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository

/**
 * @className: com.lhl.fw.rdb.helpers.Extensions
 * @description: TODO 类描述
 * @author: 78772
 * @date:  2024/7/9 13:44
 **/
@Tag(name = "Extensions", description = "Extensions")
/*inline fun <reified T : Any> Page<T>.toPageResult(): PageResult<T> {
    return PageResult(
        list = this.content,
        count = this.totalElements
    )
}*/

/**
 * 使用EntityGraph,用来做惰性加载
 *
 * @param T
 * @param ID
 * @param id
 * @param graphName
 * @return
 */
inline fun <reified T : Any, ID : Any> JpaRepository<T, ID>.findWithGraphById(
    id: ID,
    graphName: String,
): T? {
    val entityManager: EntityManager = SpringUtil.getBean(EntityManager::class.java)
    return entityManager.find(
        T::class.java,
        id,
        HashMap<String, Any>().apply {
            put(
                "jakarta.persistence.fetchgraph",
                entityManager.getEntityGraph(graphName)
            )
        }
    )
}
/**
 * 动态加载关联属性的通用扩展函数
 * @param id 主键 ID
 * @param attributePaths 需要立即加载的关联属性名（支持多个）
 */
inline fun <reified T : Any, ID : Any> JpaRepository<T, ID>.findByIdWithGraph(
    id: ID,
    attributePaths: List<String>
): T? {
    val entityManager = SpringUtil.getBean(EntityManager::class.java)

    // 创建一个动态实体图
    val entityGraph = entityManager.createEntityGraph(T::class.java)
    attributePaths.forEach { path ->
        entityGraph.addAttributeNodes(path)
    }

    // 使用 fetchgraph 属性进行查询
    val properties = mapOf(
        "jakarta.persistence.fetchgraph" to entityGraph
    )

    return entityManager.find(T::class.java, id, properties)
}

/**
 * TODO
 *
 * @param T
 * @param ID
 * @param t
 * @return
 */
fun <T : Any, ID : Any> JpaRepository<T, ID>.contains(t: T): Boolean =
    SpringUtil.getBean(EntityManager::class.java).contains(t)

/**
 * 任意对象扩展,判断实体状态的方法
 *
 * @return
 */
fun Any.isPersisted(): Boolean = SpringUtil.getBean(EntityManager::class.java).contains(this)
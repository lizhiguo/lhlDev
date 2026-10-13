package com.lhl.common.data.jpa.helper

import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import kotlin.reflect.KProperty1

// ==================== 辅助方法 ====================

/**
 * 关联辅助函数：允许通过属性引用进行表关联
 * @param prop 要关联的属性
 * @return 关联对象 Join
 */
fun <Z, T, R> From<Z, T>.join(prop: KProperty1<T, R?>): Join<T, R> = this.join<T, R>(prop.name)

/**
 * 路径获取辅助函数：允许通过属性引用获取属性路径
 * @param prop 要获取的属性
 * @return 属性路径 Path
 */
fun <R> Path<*>.get(prop: KProperty1<*, R?>): Path<R> = this.get<R>(prop.name)

// ==================== Specification 构建器 ====================

/**
 * 创建 Specification 的便捷方法，使 CriteriaBuilder 隐式可用
 * @param makePredicate 谓词构建函数，使用 CriteriaBuilder 和 Root 生成 Predicate
 * @return 构建好的 Specification
 */
fun <T : Any> where(makePredicate: CriteriaBuilder.(Root<T>) -> Predicate): Specification<T> =
    Specification { root, _, criteriaBuilder -> criteriaBuilder.makePredicate(root) }

// ==================== 内部辅助函数 ====================

/**
 * 定义 Specification 的内部辅助函数
 * 接收属性路径并传递给 CriteriaBuilder
 * @param makePredicate 谓词构建函数
 * @return 构建好的 Specification
 */
private fun <T : Any, R> KProperty1<T, R?>.spec(makePredicate: CriteriaBuilder.(path: Path<R>) -> Predicate): Specification<T> =
    let { property -> where { root -> makePredicate(root.get(property)) } }

// ==================== 相等性比较 ====================

/**
 * 相等条件：属性等于指定值
 * @param x 要比较的值
 * @return 相等条件 Specification
 */
fun <T : Any, R> KProperty1<T, R?>.equal(x: R): Specification<T> = spec { equal(it, x) }

/**
 * 不等条件：属性不等于指定值
 * @param x 要比较的值
 * @return 不等条件 Specification
 */
fun <T : Any, R> KProperty1<T, R?>.notEqual(x: R): Specification<T> = spec { notEqual(it, x) }

/**
 * IN 条件：属性值在集合中
 * 注意：如果集合为空，则忽略该条件，否则会生成永远不匹配的 IN 谓词
 * @param values 要比较的值集合
 * @return IN 条件 Specification
 */
fun <T : Any, R : Any> KProperty1<T, R?>.`in`(values: Collection<R>): Specification<T> = if (values.isNotEmpty()) spec { path ->
    `in`(path).apply { values.forEach { this.value(it) } }
} else Specification { _, _, _ -> null }

// ==================== 数值比较 ====================

/**
 * 小于等于条件：属性值小于等于指定数值
 * @param x 要比较的数值
 * @return 小于等于条件 Specification
 */
fun <T: Any> KProperty1<T, Number?>.le(x: Number) = spec { le(it, x) }

/**
 * 小于条件：属性值小于指定数值
 * @param x 要比较的数值
 * @return 小于条件 Specification
 */
fun <T: Any> KProperty1<T, Number?>.lt(x: Number) = spec { lt(it, x) }

/**
 * 大于等于条件：属性值大于等于指定数值
 * @param x 要比较的数值
 * @return 大于等于条件 Specification
 */
fun <T: Any> KProperty1<T, Number?>.ge(x: Number) = spec { ge(it, x) }

/**
 * 大于条件：属性值大于指定数值
 * @param x 要比较的数值
 * @return 大于条件 Specification
 */
fun <T: Any> KProperty1<T, Number?>.gt(x: Number) = spec { gt(it, x) }

// ==================== 可比较对象比较 ====================

/**
 * 小于条件：属性值小于指定值（适用于可比较类型）
 * @param x 要比较的值
 * @return 小于条件 Specification
 */
fun <T: Any, R : Comparable<R>> KProperty1<T, R?>.lessThan(x: R) = spec { lessThan(it, x) }

/**
 * 小于等于条件：属性值小于等于指定值（适用于可比较类型）
 * @param x 要比较的值
 * @return 小于等于条件 Specification
 */
fun <T: Any, R : Comparable<R>> KProperty1<T, R?>.lessThanOrEqualTo(x: R) = spec { lessThanOrEqualTo(it, x) }

/**
 * 大于条件：属性值大于指定值（适用于可比较类型）
 * @param x 要比较的值
 * @return 大于条件 Specification
 */
fun <T: Any, R : Comparable<R>> KProperty1<T, R?>.greaterThan(x: R) = spec { greaterThan(it, x) }

/**
 * 大于等于条件：属性值大于等于指定值（适用于可比较类型）
 * @param x 要比较的值
 * @return 大于等于条件 Specification
 */
fun <T: Any, R : Comparable<R>> KProperty1<T, R?>.greaterThanOrEqualTo(x: R) = spec { greaterThanOrEqualTo(it, x) }

/**
 * 区间条件：属性值在指定范围内（适用于可比较类型）
 * @param x 起始值
 * @param y 结束值
 * @return 区间条件 Specification
 */
fun <T: Any, R : Comparable<R>> KProperty1<T, R?>.between(x: R, y: R) = spec { between(it, x, y) }

// ==================== 布尔类型判断 ====================

/**
 * 为真条件：布尔属性值为 true
 * @return 为真条件 Specification
 */
fun <T: Any> KProperty1<T, Boolean?>.isTrue() = spec { isTrue(it) }

/**
 * 为假条件：布尔属性值为 false
 * @return 为假条件 Specification
 */
fun <T: Any> KProperty1<T, Boolean?>.isFalse() = spec { isFalse(it) }

// ==================== 空值判断 ====================

/**
 * 为空条件：属性值为 null
 * @return 为空条件 Specification
 */
fun <T: Any, R> KProperty1<T, R?>.isNull() = spec { isNull(it) }

/**
 * 非空条件：属性值不为 null
 * @return 非空条件 Specification
 */
fun <T: Any, R> KProperty1<T, R?>.isNotNull() = spec { isNotNull(it) }

// ==================== 集合判断 ====================

/**
 * 空集合条件：集合属性为空
 * @return 空集合条件 Specification
 */
fun <T : Any, R : Collection<*>> KProperty1<T, R?>.isEmpty() = spec { isEmpty(it) }

/**
 * 非空集合条件：集合属性不为空
 * @return 非空集合条件 Specification
 */
fun <T : Any, R : Collection<*>> KProperty1<T, R?>.isNotEmpty() = spec { isNotEmpty(it) }

/**
 * 包含元素条件：集合包含指定元素
 * @param elem 要检查的元素
 * @return 包含元素条件 Specification
 */
fun <T : Any, E, R : Collection<E>> KProperty1<T, R?>.isMember(elem: E) = spec { isMember(elem, it) }

/**
 * 不包含元素条件：集合不包含指定元素
 * @param elem 要检查的元素
 * @return 不包含元素条件 Specification
 */
fun <T : Any, E, R : Collection<E>> KProperty1<T, R?>.isNotMember(elem: E) = spec { isNotMember(elem, it) }

// ==================== 字符串匹配 ====================

/**
 * LIKE 条件：字符串属性匹配指定模式
 * @param x 匹配模式（支持 SQL 通配符 % 和 _）
 * @return LIKE 条件 Specification
 */
fun <T : Any> KProperty1<T, String?>.like(x: String): Specification<T> = spec { like(it, x) }

/**
 * LIKE 条件（带转义符）：字符串属性匹配指定模式
 * @param x 匹配模式（支持 SQL 通配符 % 和 _）
 * @param escapeChar 转义字符
 * @return LIKE 条件 Specification
 */
fun <T : Any> KProperty1<T, String?>.like(x: String, escapeChar: Char): Specification<T> = spec { like(it, x, escapeChar) }

/**
 * NOT LIKE 条件：字符串属性不匹配指定模式
 * @param x 匹配模式（支持 SQL 通配符 % 和 _）
 * @return NOT LIKE 条件 Specification
 */
fun <T : Any> KProperty1<T, String?>.notLike(x: String): Specification<T> = spec { notLike(it, x) }

/**
 * NOT LIKE 条件（带转义符）：字符串属性不匹配指定模式
 * @param x 匹配模式（支持 SQL 通配符 % 和 _）
 * @param escapeChar 转义字符
 * @return NOT LIKE 条件 Specification
 */
fun <T : Any> KProperty1<T, String?>.notLike(x: String, escapeChar: Char): Specification<T> = spec { notLike(it, x, escapeChar) }

// ==================== 逻辑运算符 ====================

/**
 * AND 运算符：中缀函数，组合两个 Specification 为 AND 关系
 * @param other 另一个 Specification
 * @return 组合后的 AND 条件 Specification
 */
infix fun <T : Any> Specification<T>.and(other: Specification<T>): Specification<T> = this.and(other)!!

/**
 * AND 运算符：组合多个 Specification 为 AND 关系（可变参数版本）
 * @param specs Specification 可变参数数组
 * @return 组合后的 AND 条件 Specification
 */
inline fun <reified T : Any> and(vararg specs: Specification<T>?): Specification<T> {
    return and(specs.toList())
}

/**
 * AND 运算符：组合多个 Specification 为 AND 关系（可迭代集合版本）
 * @param specs Specification 可迭代集合
 * @return 组合后的 AND 条件 Specification
 */
inline fun <reified T : Any> and(specs: Iterable<Specification<T>?>): Specification<T> {
    return combineSpecification(specs, Specification<T>::and)
}

/**
 * OR 运算符：中缀函数，组合两个 Specification 为 OR 关系
 * @param other 另一个 Specification
 * @return 组合后的 OR 条件 Specification
 */
infix fun <T : Any> Specification<T>.or(other: Specification<T>): Specification<T> = this.or(other)!!

/**
 * OR 运算符：组合多个 Specification 为 OR 关系（可变参数版本）
 * @param specs Specification 可变参数数组
 * @return 组合后的 OR 条件 Specification
 */
inline fun <reified T : Any> or(vararg specs: Specification<T>?): Specification<T> {
    return or(specs.toList())
}

/**
 * OR 运算符：组合多个 Specification 为 OR 关系（可迭代集合版本）
 * @param specs Specification 可迭代集合
 * @return 组合后的 OR 条件 Specification
 */
inline fun <reified T : Any> or(specs: Iterable<Specification<T>?>): Specification<T> {
    return combineSpecification(specs, Specification<T>::or)
}

/**
 * NOT 运算符：取反 Specification 条件
 * @return 取反后的 Specification
 */
operator fun <T : Any> Specification<T>.not(): Specification<T> = Specification.not(this)

/**
 * Specification 组合工具函数
 * 使用指定的操作符组合多个 Specification
 * @param specs Specification 可迭代集合
 * @param operation 组合操作（AND 或 OR）
 * @return 组合后的 Specification
 */
inline fun <reified T : Any> combineSpecification(specs: Iterable<Specification<T>?>,
                                                  operation: Specification<T>.(Specification<T>) -> Specification<T>): Specification<T> {
    return specs.filterNotNull().fold(emptySpecification()) { existing, new -> existing.operation(new) }
}

/**
 * 空 Specification：不添加任何过滤条件
 * @return 空条件的 Specification
 */
inline fun <reified T : Any> emptySpecification(): Specification<T> = Specification { _, _, _ -> null }

/**
 * 嵌套查询条件：创建嵌套查询条件
 * @param prop 嵌套属性
 * @return 嵌套查询条件函数
 */
fun <T : Any, R1, R2> KProperty1<T, R1?>.nested(
    prop: KProperty1<R1, R2?>
): (R2) -> Specification<T> = { value ->
    where { root ->
        equal(root.get(this@nested).get(prop), value)
    }
}

/**
 *
 */
fun <T : Any, R1, R2 : Number> KProperty1<T, R1?>.nestedGt(
    prop: KProperty1<R1, R2?>,
    value: R2
): Specification<T> = where { root ->
    gt(root.get(this@nestedGt).get(prop), value)
}

/**
 * 嵌套查询条件：创建嵌套查询条件
 */
fun <T : Any, R1, R2 : Number> KProperty1<T, R1?>.nestedGe(
    prop: KProperty1<R1, R2?>,
    value: R2
): Specification<T> = where { root ->
    ge(root.get(this@nestedGe).get(prop), value)
}
/******************复杂查询********************/
/*
val shows = tvShowRepo.findAll(
    or(
        and(
            availableOnNetflix(false),
            hasKeywordIn(listOf("Jimmy"))
        ),
        and(
            availableOnNetflix(true),
            or(
                hasKeyword("killer"),
                hasKeyword("monster")
            )
        )
    )
)*/
/******************嵌套查询********************/
/*
object PersonSpec {

    // 定义嵌套属性查询
    private val byName = Person::name.nested(Username::value)
    private val byId   = Person::id.nested(Person.PersonId::value)

    fun uniqueCheck(
        name: String?,
        excludeId: Person.PersonId?
    ): Specification<Person> {
        val _name = name?.let       { byName(it) }
        val _notId = excludeId?.let { !byId(it.value) }  // ← not 取反
        return and(_name, _notId)
    }
}*/

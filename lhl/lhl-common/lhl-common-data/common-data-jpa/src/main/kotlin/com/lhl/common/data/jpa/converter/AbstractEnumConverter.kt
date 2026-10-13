package com.lhl.common.data.jpa.converter

import jakarta.persistence.AttributeConverter

/**
 * 通用枚举转换器抽象基类
 * 用于JPA实体中枚举类型与数据库字段值的双向转换
 *
 * @param T 枚举类型，必须是Enum的子类
 * @param V 数据库字段类型，通常是String或Int等基本类型
 * @property enumType 枚举类的Class对象
 * @property valueGetter 从枚举获取数据库值的转换函数
 * @property fromValue 从数据库值获取枚举的转换函数
 *
 * @example
 * ```kotlin
 * // 示例：将Status枚举转换为String类型的状态码
 * class StatusConverter : AbstractEnumConverter<Status, String>(
 *     Status::class.java,
 *     { it.code },        // 枚举 -> 数据库值
 *     { Status.from(it) } // 数据库值 -> 枚举
 * )
 * ```
 */
abstract class AbstractEnumConverter<T : Enum<T>, V>(
    private val enumType: Class<T>,
    private val valueGetter: (T) -> V,
    private val fromValue: (V) -> T
) : AttributeConverter<T, V> {

    /**
     * 将枚举值转换为数据库列值
     *
     * @param attribute 实体类中的枚举值，可能为null
     * @return 转换后的数据库值，如果输入为null则返回null
     */
    override fun convertToDatabaseColumn(attribute: T?): V? {
        return attribute?.let(valueGetter)
    }

    /**
     * 将数据库列值转换为枚举值
     *
     * @param dbData 数据库中的值，可能为null
     * @return 转换后的枚举值，如果输入为null则返回null
     */
    override fun convertToEntityAttribute(dbData: V?): T? {
        return dbData?.let { value ->
            runCatching { fromValue(value) }.getOrElse {
                throw IllegalArgumentException(
                    "无法将值 '$value' 转换为枚举 ${enumType.simpleName}"  // 这里用到了
                )
            }
        }
    }
}
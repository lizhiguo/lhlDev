package com.lhl.common.data.jpa.helper

import java.lang.reflect.Field
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

object EntityUtil {
    /**
     * 复制同类型属性值操作, dest字段值 = src字段值
     */
    fun <T1 : Any, T2 : Any> copyVal(dest: T1, src: T2, vararg excludeFields: String): T1 {
        return if (dest::class == src::class) {
            copyValCommon(dest, src, CopyType.ALL, excludeFields)
        } else {
            copyValCommonOfDiffObj(dest, src, CopyType.ALL, excludeFields)
        }
    }

    /**
     * 复制同类型属性值操作, dest字段值 = src字段值  (仅当dest字段值为 <null> 时才拷贝)
     */
    fun <T1 : Any, T2 : Any> copyValOnlyDestNull(dest: T1, src: T2, vararg excludeFields: String): T1 {
        return if (dest::class == src::class) {
            copyValCommon(dest, src, CopyType.ONLY_DEST_NULL, excludeFields)
        } else {
            copyValCommonOfDiffObj(dest, src, CopyType.ONLY_DEST_NULL, excludeFields)
        }
    }

    /**
     * 复制同类型属性值操作, dest字段值 = src字段值  (仅当dest字段值为 <null> 或 <空> 时才拷贝)
     */
    fun <T1 : Any, T2 : Any> copyValOnlyDestEmpty(dest: T1, src: T2, vararg excludeFields: String): T1 {
        return if (dest::class == src::class) {
            copyValCommon(dest, src, CopyType.ONLY_DEST_EMPTY, excludeFields)
        } else {
            copyValCommonOfDiffObj(dest, src, CopyType.ONLY_DEST_EMPTY, excludeFields)
        }
    }

    /**
     * 同类型两对象复制属性值操作
     */
    private fun <T1 : Any, T2 : Any> copyValCommon(
        dest: T1,
        src: T2,
        copyType: CopyType,
        excludeFields: Array<out String>?,
    ): T1 {
        val kClass = dest::class
        if (kClass != src::class) return dest
        val jClass = kClass.java
        val members = kClass.memberProperties
        var srcValTmp: Any?
        var destValTmp: Any?
        var kPropertyName: String?
        var destFieldMap: Map<String, Field>? = null
        for (kProperty in members) {
            if ("PUBLIC" != kProperty.visibility.toString()) continue
            kPropertyName = kProperty.name
            if (excludeFields != null && excludeFields.contains(kPropertyName)) continue
            destValTmp = kProperty.call(dest)
            srcValTmp = kProperty.call(src)
            if ((destValTmp == null && srcValTmp == null) || (destValTmp == srcValTmp)) continue
            if (copyType == CopyType.ALL ||
                (copyType == CopyType.ONLY_DEST_NULL && destValTmp == null) ||
                (copyType == CopyType.ONLY_DEST_EMPTY && ((destValTmp == null) || (destValTmp is String && "" == destValTmp.trim())))
            ) {
                if (destFieldMap == null) {
                    destFieldMap = declareFields(jClass)
                }
                val jField = destFieldMap[kPropertyName] ?: continue
                val bakAccessible = jField.trySetAccessible()
                jField.isAccessible = true
                jField.set(dest, srcValTmp)
                jField.isAccessible = bakAccessible
            }
        }
        return dest
    }

    private fun <T1 : Any, T2 : Any> copyValCommonOfDiffObj(
        dest: T1,
        src: T2,
        copyType: CopyType,
        excludeFields: Array<out String>?,
    ): T1 {
        val kDestClass = dest::class
        val kSrcClass = src::class
        val jDestClass = kDestClass.java
        val destMembers = kDestClass.memberProperties
        val mapOfSrcProperties: MutableMap<String, KProperty1<out T2, Any?>> = mutableMapOf()
        kSrcClass.memberProperties.forEach { mapOfSrcProperties[it.name] = it }
        var srcValTmp: Any?
        var destValTmp: Any?
        var kPropertyName: String?
        var destFieldMap: Map<String, Field>? = null
        for (destKProperty in destMembers) {
            if ("PUBLIC" != destKProperty.visibility.toString()) continue
            kPropertyName = destKProperty.name
            if (excludeFields != null && excludeFields.contains(kPropertyName)) continue
            val srcKProperty = mapOfSrcProperties[kPropertyName] ?: continue
            if (srcKProperty.returnType != destKProperty.returnType) continue // 同名不同类型
            destValTmp = destKProperty.call(dest)
            srcValTmp = srcKProperty.call(src)
            if ((destValTmp == null && srcValTmp == null) || (destValTmp == srcValTmp)) continue
            if (copyType == CopyType.ALL ||
                (copyType == CopyType.ONLY_DEST_NULL && destValTmp == null) ||
                (copyType == CopyType.ONLY_DEST_EMPTY && ((destValTmp == null) || (destValTmp is String && "" == destValTmp.trim())))
            ) {
                /**
                 * 赋值
                 */
                if (destFieldMap == null) {
                    destFieldMap = declareFields(jDestClass)
                }
                val jDestField = destFieldMap[kPropertyName] ?: continue
                val bakAccessible = jDestField.trySetAccessible()
                jDestField.isAccessible = true
                jDestField.set(dest, srcValTmp)
                jDestField.isAccessible = bakAccessible
            }
        }
        return dest
    }

    private fun declareFields(cls: Class<*>): MutableMap<String, Field> {
        val fs = mutableMapOf<String, Field>()
        var clsTmp = cls
        do {
            clsTmp.declaredFields.forEach {
                fs.putIfAbsent(it.name, it)
            }
            clsTmp = clsTmp.superclass
        } while (clsTmp != objCls)
        return fs
    }

    private val objCls = Object::class.java

    private enum class CopyType {
        /**
         * 拷贝所有字段值
         */
        ALL,

        /**
         * 当目标类字段值为null时, 拷贝来源字段值
         */
        ONLY_DEST_NULL,

        /**
         * 当目标类字段值为null或""时, 拷贝来源字段值
         */
        ONLY_DEST_EMPTY,
    }
}

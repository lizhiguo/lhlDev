package com.lhl.common.core.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.tags.Tag
import java.text.MessageFormat

/**
 * 基础消息接口
 *
 * 用途说明:
 * - 定义了 code(错误码/成功码) 和 msg(消息模板) 两个核心属性
 * - 支持 {0}、{1} 占位符格式化,比 %s/%d 更安全
 * - 是 IBaseErrorCode 和 IBaseSuccessMsg 的基础接口
 *
 * 实现示例:
 * - 错误码: DictErrorCode, CommonError 实现 IBaseErrorCode
 * - 成功消息: Success.OK, Success.CREATED 实现 IBaseSuccessMsg
 */
@Tag(name = "Base", description = "Base")
interface IBaseMsg {
    val code: String
    val msg: String

    /** 支持 {0}、{1} 占位符，更现代、更安全（不会因 %s 和 %d 混用抛异常） */
    fun format(vararg args: Any?): String =
        if (args.isEmpty()) msg else MessageFormat.format(msg, *args)
}

/**
 * 错误码特化接口
 *
 * 用途说明:
 * - 专门用于异常系统的错误码定义
 * - 继承自 IBaseMsg,保持了消息格式化能力
 * - 类型安全:异常类只能接受 IBaseErrorCode,防止误用成功消息
 *
 * 实现示例: DictErrorCode, CommonError, ParamErrorCode
 */
interface IBaseErrorCode : IBaseMsg

/**
 * 成功消息特化接口
 *
 * 用途说明:
 * - 用于成功响应的消息定义
 * - 与 IBaseErrorCode 分离,避免语义混淆
 *
 * 实现示例: Success.OK, Success.CREATED, Success.ACCEPTED
 */
interface IBaseSuccessMsg : IBaseMsg

/**
 * @className: com.lhl.fw.rdb.helpers.Enums
 * @description: 枚举基类
 * @author: 78772
 * @date:  2024/12/18 10:25
 **/
@Tag(name = "Enums", description = "Enums")
interface BaseEnum<K> {
    fun getCode(): K
    fun getDisplayName(): String
}

interface StringEnum<E: Enum<E>> {
    fun fromString(value: String): E
}

/**
 * 通用枚举，所有枚举都必须实现该接口
 * @author Walker
 * date 2025-05-26
 */
interface IBaseEnum<T> {

    /** 字典值 */
    val value: T

    /** 字典名称 */
    val name: String

    /** 字典描述 */
    val description: String

    companion object {

        /* ----------  下面几个工具方法完全对标原来的 static 方法  ---------- */

        @JvmStatic
        inline fun <reified E, T> getEnum(value: T, clazz: Class<E>): E?
                where E : Enum<E>, E : IBaseEnumJson<T> {
            requireNotNull(value)
            return enumValues<E>().firstOrNull { it.value == value }
        }

        @JvmStatic
        inline fun <reified E, T> getName(value: T, clazz: Class<E>): String?
                where E : Enum<E>, E : IBaseEnumJson<T> {
            requireNotNull(value)
            return enumValues<E>().firstOrNull { it.value == value }?.name
        }

        @JvmStatic
        inline fun <reified E, T> getCode(name: String, clazz: Class<E>): T?
                where E : Enum<E>, E : IBaseEnumJson<T> {
            requireNotNull(name)
            return enumValues<E>().firstOrNull { it.name == name }?.value
        }

        @JvmStatic
        inline fun <reified E, T> getEnums(clazz: Class<E>): List<E>
                where E : Enum<E>, E : IBaseEnumJson<T> =
            enumValues<E>().toList()
    }
}
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
interface IBaseEnumJson<T>: IBaseEnum<T>{
    /**
     * 获取枚举json
     * @return
     */
    fun getJson(): String
}

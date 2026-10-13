package com.lhl.demo.dict.app.command

import com.lhl.demo.dict.domain.model.DictTypeAttr

/**
 * 字典类型写命令（CQRS Command）
 *
 * 普通密封类（未使用 @Command 注解，以减少对 jmolecules-events 的额外依赖）；
 * 每个命令在同文件定义 [toAttr] 扩展函数完成 Command → Attr 转换。
 */
sealed class DictTypeCmd {
    data class Create(
        val dictCode: String?,
        val name: String?,
        val description: String? = null,
        val status: String? = null,
    ) : DictTypeCmd()

    data class Modify(
        val entityId: Long,
        val name: String? = null,
        val description: String? = null,
        val status: String? = null,
    ) : DictTypeCmd()

    data class Delete(val entityId: Long) : DictTypeCmd()

    data class ChangeStatus(val entityId: Long, val status: String) : DictTypeCmd()
}

/** Create 命令 → 领域 Attr */
fun DictTypeCmd.Create.toAttr() = DictTypeAttr(
    dictCode = dictCode,
    name = name,
    description = description,
    status = status,
)

/** Modify 命令 → 领域 Attr（仅覆盖非 null 字段） */
fun DictTypeCmd.Modify.toAttr() = DictTypeAttr(
    name = name,
    description = description,
    status = status,
)

/** ChangeStatus 命令 → 领域 Attr（仅状态） */
fun DictTypeCmd.ChangeStatus.toAttr() = DictTypeAttr(status = status)

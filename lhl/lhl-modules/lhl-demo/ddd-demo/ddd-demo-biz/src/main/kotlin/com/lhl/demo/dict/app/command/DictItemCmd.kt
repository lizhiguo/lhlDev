package com.lhl.demo.dict.app.command

import com.lhl.demo.dict.domain.model.DictItemAttr

/**
 * 字典项写命令（CQRS Command）
 */
sealed class DictItemCmd {
    data class Create(
        val dictTypeCode: String?,
        val itemCode: String?,
        val itemName: String?,
        val sort: Int? = null,
        val status: String? = null,
        val remark: String? = null,
    ) : DictItemCmd()

    data class Modify(
        val entityId: Long,
        val itemName: String? = null,
        val sort: Int? = null,
        val status: String? = null,
        val remark: String? = null,
    ) : DictItemCmd()

    data class Delete(val entityId: Long) : DictItemCmd()

    data class ChangeStatus(val entityId: Long, val status: String) : DictItemCmd()
}

fun DictItemCmd.Create.toAttr() = DictItemAttr(
    dictTypeCode = dictTypeCode,
    itemCode = itemCode,
    itemName = itemName,
    sort = sort,
    status = status,
    remark = remark,
)

fun DictItemCmd.Modify.toAttr() = DictItemAttr(
    itemName = itemName,
    sort = sort,
    status = status,
    remark = remark,
)

fun DictItemCmd.ChangeStatus.toAttr() = DictItemAttr(status = status)

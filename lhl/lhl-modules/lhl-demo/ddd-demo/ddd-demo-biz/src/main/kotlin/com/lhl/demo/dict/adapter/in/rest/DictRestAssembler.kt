package com.lhl.demo.dict.adapter.`in`.rest

import com.lhl.demo.dict.api.model.*
import com.lhl.demo.dict.app.command.DictItemCmd
import com.lhl.demo.dict.app.command.DictTypeCmd

/**
 * 入站装配器：请求 VO → 命令 Command
 */

fun DictTypeCreateVO.toCommand(): DictTypeCmd.Create =
    DictTypeCmd.Create(dictCode = dictCode, name = name, description = description, status = status)

fun DictTypeUpdateVO.toCommand(id: Long): DictTypeCmd.Modify =
    DictTypeCmd.Modify(entityId = id, name = name, description = description, status = status)

fun DictItemCreateVO.toCommand(): DictItemCmd.Create =
    DictItemCmd.Create(dictTypeCode, itemCode, itemName, sort, status, remark)

fun DictItemUpdateVO.toCommand(id: Long): DictItemCmd.Modify =
    DictItemCmd.Modify(entityId = id, itemName = itemName, sort = sort, status = status, remark = remark)

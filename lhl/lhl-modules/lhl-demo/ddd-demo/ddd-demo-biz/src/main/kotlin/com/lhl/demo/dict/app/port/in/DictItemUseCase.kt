package com.lhl.demo.dict.app.port.`in`

import com.lhl.demo.dict.api.model.DictItemDTO
import com.lhl.demo.dict.app.command.DictItemCmd
import org.jmolecules.architecture.hexagonal.PrimaryPort

/**
 * 字典项写操作入站端口
 */
@PrimaryPort
interface DictItemUseCase {
    fun handle(cmd: DictItemCmd.Create): DictItemDTO
    fun handle(cmd: DictItemCmd.Modify): DictItemDTO
    fun handle(cmd: DictItemCmd.Delete)
    fun handle(cmd: DictItemCmd.ChangeStatus): DictItemDTO
}

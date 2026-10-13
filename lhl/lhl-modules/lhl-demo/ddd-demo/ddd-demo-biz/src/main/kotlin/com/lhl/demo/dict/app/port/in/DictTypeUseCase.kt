package com.lhl.demo.dict.app.port.`in`

import com.lhl.demo.dict.api.model.DictTypeDTO
import com.lhl.demo.dict.app.command.DictTypeCmd
import org.jmolecules.architecture.hexagonal.PrimaryPort

/**
 * 字典类型写操作入站端口
 */
@PrimaryPort
interface DictTypeUseCase {
    fun handle(cmd: DictTypeCmd.Create): DictTypeDTO
    fun handle(cmd: DictTypeCmd.Modify): DictTypeDTO
    fun handle(cmd: DictTypeCmd.Delete)
    fun handle(cmd: DictTypeCmd.ChangeStatus): DictTypeDTO
}

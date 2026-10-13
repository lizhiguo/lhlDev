package com.lhl.demo.dict.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.lhl.common.data.jpa.converter.AbstractEnumConverter
import jakarta.persistence.Converter
import org.jmolecules.ddd.annotation.ValueObject

/**
 * 字典类型状态枚举值对象
 */
@ValueObject
enum class DictTypeStatus(val code: String, val description: String) {
    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "停用");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromCode(code: String): DictTypeStatus =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("无效的字典类型状态: $code")
    }
}

/** 字典类型状态 JPA 转换器（autoApply 全局自动应用） */
@Converter(autoApply = true)
class DictTypeStatusConverter : AbstractEnumConverter<DictTypeStatus, String>(
    DictTypeStatus::class.java,
    { it.code },
    { DictTypeStatus.fromCode(it) }
)

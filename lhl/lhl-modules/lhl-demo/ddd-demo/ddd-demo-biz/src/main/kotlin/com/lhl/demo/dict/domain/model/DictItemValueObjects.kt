package com.lhl.demo.dict.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.lhl.common.data.jpa.converter.AbstractEnumConverter
import jakarta.persistence.Converter
import org.jmolecules.ddd.annotation.ValueObject

/**
 * 字典项状态枚举值对象
 */
@ValueObject
enum class DictItemStatus(val code: String, val description: String) {
    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "停用");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromCode(code: String): DictItemStatus =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("无效的字典项状态: $code")
    }
}

/** 字典项状态 JPA 转换器（autoApply 全局自动应用） */
@Converter(autoApply = true)
class DictItemStatusConverter : AbstractEnumConverter<DictItemStatus, String>(
    DictItemStatus::class.java,
    { it.code },
    { DictItemStatus.fromCode(it) }
)

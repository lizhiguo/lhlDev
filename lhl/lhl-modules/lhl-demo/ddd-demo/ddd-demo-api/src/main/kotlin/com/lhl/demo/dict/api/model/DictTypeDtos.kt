package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema

// ========== 字典类型 写响应 DTO ==========

@Schema(name = "字典类型响应")
data class DictTypeDTO(
    val id: Long? = null,
    val dictCode: String? = null,
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val statusDesc: String? = null,
    var createTime: String? = null,
    var updateTime: String? = null,
)

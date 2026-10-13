package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema

// ========== 字典项 写响应 DTO ==========

@Schema(name = "字典项响应")
data class DictItemDTO(
    val id: Long? = null,
    val dictTypeCode: String? = null,
    val itemCode: String? = null,
    val itemName: String? = null,
    val sort: Int? = null,
    val status: String? = null,
    val statusDesc: String? = null,
    val remark: String? = null,
    var createTime: String? = null,
    var updateTime: String? = null,
)

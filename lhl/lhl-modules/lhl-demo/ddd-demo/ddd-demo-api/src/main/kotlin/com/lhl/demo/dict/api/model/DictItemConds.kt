package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema

// ========== 字典项 查询条件 ==========

@Schema(name = "字典项查询条件")
data class DictItemCond(
    val dictTypeCode: String? = null,
    val itemCode: String? = null,
    val itemName: String? = null,
    val status: String? = null,
)

package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema

// ========== 字典类型 查询条件 ==========

@Schema(name = "字典类型查询条件")
data class DictTypeCond(
    val dictCode: String? = null,
    val name: String? = null,
    val status: String? = null,
)

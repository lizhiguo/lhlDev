package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema

// ========== 字典类型 读响应 View ==========

@Schema(name = "字典类型视图")
data class DictTypeView(
    val id: Long? = null,
    val dictCode: String? = null,
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val statusDesc: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
)

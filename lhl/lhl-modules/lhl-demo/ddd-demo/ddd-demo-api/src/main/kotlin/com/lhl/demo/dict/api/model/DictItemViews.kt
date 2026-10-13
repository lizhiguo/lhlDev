package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema

// ========== 字典项 读响应 View ==========

@Schema(name = "字典项视图")
data class DictItemView(
    val id: Long? = null,
    val dictTypeCode: String? = null,
    val itemCode: String? = null,
    val itemName: String? = null,
    val sort: Int? = null,
    val status: String? = null,
    val statusDesc: String? = null,
    val remark: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
)

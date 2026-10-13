package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

// ========== 字典项 请求 VO ==========

@Schema(name = "字典项创建请求")
data class DictItemCreateVO(
    @field:Schema(description = "所属字典类型编码", example = "gender")
    @field:NotBlank(message = "字典类型编码不能为空")
    val dictTypeCode: String? = null,

    @field:Schema(description = "字典项编码，同一类型下唯一", example = "male")
    @field:NotBlank(message = "字典项编码不能为空")
    val itemCode: String? = null,

    @field:Schema(description = "字典项显示名称", example = "男")
    @field:NotBlank(message = "字典项名称不能为空")
    val itemName: String? = null,

    @field:Schema(description = "排序号，越小越靠前", example = "1")
    val sort: Int? = null,

    @field:Schema(description = "状态：ENABLED 启用 / DISABLED 停用", example = "ENABLED")
    val status: String? = null,

    @field:Schema(description = "备注", example = "男性")
    val remark: String? = null,
)

@Schema(name = "字典项更新请求")
data class DictItemUpdateVO(
    @field:Schema(description = "字典项显示名称", example = "男")
    val itemName: String? = null,

    @field:Schema(description = "排序号", example = "1")
    val sort: Int? = null,

    @field:Schema(description = "状态：ENABLED 启用 / DISABLED 停用", example = "ENABLED")
    val status: String? = null,

    @field:Schema(description = "备注", example = "男性")
    val remark: String? = null,
)

package com.lhl.demo.dict.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

// ========== 字典类型 请求 VO ==========

@Schema(name = "字典类型创建请求")
data class DictTypeCreateVO(
    @field:Schema(description = "字典类型编码，全局唯一", example = "gender")
    @field:NotBlank(message = "字典类型编码不能为空")
    val dictCode: String? = null,

    @field:Schema(description = "字典类型名称", example = "性别")
    @field:NotBlank(message = "字典类型名称不能为空")
    val name: String? = null,

    @field:Schema(description = "字典类型描述", example = "人员性别字典")
    val description: String? = null,

    @field:Schema(description = "状态：ENABLED 启用 / DISABLED 停用", example = "ENABLED")
    val status: String? = null,
)

@Schema(name = "字典类型更新请求")
data class DictTypeUpdateVO(
    @field:Schema(description = "字典类型名称", example = "性别")
    val name: String? = null,

    @field:Schema(description = "字典类型描述", example = "人员性别字典")
    val description: String? = null,

    @field:Schema(description = "状态：ENABLED 启用 / DISABLED 停用", example = "ENABLED")
    val status: String? = null,
)

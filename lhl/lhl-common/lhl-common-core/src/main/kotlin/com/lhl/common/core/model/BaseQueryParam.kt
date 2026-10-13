package com.lhl.common.core.model

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

/**
 * 数据查询通用vo
 */
@Schema(name = "基本查询数据vo", description = "基础通用查询数据")
abstract class BaseQueryParam : Serializable {
    @Schema(name = "主键", description = "主键", hidden = true)
    var id: Long? = null

    @Schema(name = "版本", description = "版本", hidden = true)
    var version: Long? = 0

    @Schema(name = "页码", description = "页码", hidden = true)
    var pageNo: Int? = null

    @Schema(name = "分页条数", description = "分页条数", hidden = true)
    var pageSize: Int? = null

    @Schema(name = "总记录数", description = "总记录数,避免分页查询多次计算总行数,提高性能", hidden = true)
    var recordsTotal: Long? = null

    @Schema(name = "扩展Map对象", description = "扩展map对象")
    var params: Map<String, Any>? = null
}
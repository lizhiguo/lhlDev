package com.lhl.common.core.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import tools.jackson.databind.node.ObjectNode
import java.io.Serializable
import java.time.LocalDateTime

@Schema(name = "基本数据DTO", description = "基础通用数据")
abstract class BaseDTO : Serializable {
    @field:Schema(name = "主键", description = "主键", hidden = true)
    var id: Long? = 0

    @field:Schema(description = "创建时间", hidden = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var createTime: LocalDateTime? = null

    @field:Schema(description = "更新时间", hidden = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    var updateTime: LocalDateTime? = null

    @field:Schema(description = "创建人", hidden = true)
    var createBy: String? = null

    @field:Schema(description = "更新人", hidden = true)
    var updateBy: String? = null

    @field:Schema(description = "版本", hidden = true)
    var version: Long? = 0

    @field:Schema(description = "扩展json对象", hidden = true)
    var extJsonObject: ObjectNode? = null
    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is BaseDTO) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

// 通用的分页返回容器
data class PageResult<T>(
    val total: Long,        // 总记录数
    val pages: Int,         // 总页数
    val current: Int,       // 当前页码
    val size: Int,          // 每页条数
    val items: List<T>    // 数据列表
){
    companion object {
        fun <T> empty(): PageResult<T> = PageResult(0, 0, 1, 10, emptyList())
    }
}

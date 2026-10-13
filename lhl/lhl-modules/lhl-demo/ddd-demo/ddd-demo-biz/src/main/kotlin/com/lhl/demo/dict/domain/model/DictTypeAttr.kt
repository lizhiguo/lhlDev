package com.lhl.demo.dict.domain.model

/**
 * 字典类型领域输入对象（Attr）
 *
 * 所有字段可空以支持局部更新；不依赖 Command / VO，是领域层的纯净输入。
 * 字典类字段在此使用 String，由 [toEntity] 转换为对应的枚举。
 */
data class DictTypeAttr(
    val dictCode: String? = null,
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
)

/** 新建实体：将 Attr 转换为 [DictType] 实例 */
fun DictTypeAttr.toEntity(): DictType = DictType().also {
    it.dictCode = this.dictCode
    it.name = this.name
    it.description = this.description
    it.status = this.status?.let { s -> DictTypeStatus.fromCode(s) }
}

/** 局部更新：仅覆盖非 null 字段到已有实体 */
fun DictTypeAttr.updateEntity(entity: DictType) {
    this.dictCode?.let { entity.dictCode = it }
    this.name?.let { entity.name = it }
    this.description?.let { entity.description = it }
    this.status?.let { entity.status = DictTypeStatus.fromCode(it) }
}

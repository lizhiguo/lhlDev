package com.lhl.demo.dict.domain.model

/**
 * 字典项领域输入对象（Attr）
 *
 * 所有字段可空以支持局部更新；字典类字段使用 String，由 [toEntity] 转换为枚举。
 */
data class DictItemAttr(
    val dictTypeCode: String? = null,
    val itemCode: String? = null,
    val itemName: String? = null,
    val sort: Int? = null,
    val status: String? = null,
    val remark: String? = null,
)

/** 新建实体：将 Attr 转换为 [DictItem] 实例 */
fun DictItemAttr.toEntity(): DictItem = DictItem().also {
    it.dictTypeCode = this.dictTypeCode
    it.itemCode = this.itemCode
    it.itemName = this.itemName
    it.sort = this.sort
    it.status = this.status?.let { s -> DictItemStatus.fromCode(s) }
    it.remark = this.remark
}

/** 局部更新：仅覆盖非 null 字段到已有实体 */
fun DictItemAttr.updateEntity(entity: DictItem) {
    this.dictTypeCode?.let { entity.dictTypeCode = it }
    this.itemCode?.let { entity.itemCode = it }
    this.itemName?.let { entity.itemName = it }
    this.sort?.let { entity.sort = it }
    this.status?.let { entity.status = DictItemStatus.fromCode(it) }
    this.remark?.let { entity.remark = it }
}

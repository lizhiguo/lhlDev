package com.lhl.demo.dict.domain.model

import com.lhl.common.core.helper.SnowflakeGeneratorHelper
import com.lhl.common.data.jpa.model.entity.AbstractAuditableJpaEntity
import com.lhl.demo.dict.domain.event.DictItemEvent
import jakarta.persistence.*
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.types.Identifier
import java.io.Serializable

/**
 * 字典项聚合根
 *
 * 表示某个字典类型下的具体选项（如"性别"下的"男/女"）。
 * 与 [DictType] 通过 [dictTypeCode] 关联，同一类型下 [itemCode] 唯一。
 */
@AggregateRoot
@Entity
@DynamicInsert
@DynamicUpdate
@Table(
    name = "dict_item",
    uniqueConstraints = [UniqueConstraint(name = "uk_dict_item_type_code", columnNames = ["dict_type_code", "item_code"])],
    comment = "字典项"
)
class DictItem(
    @EmbeddedId
    override var id: DictItemId? = null,

    @Column(name = "dict_type_code", nullable = false, length = 50, comment = "所属字典类型编码")
    var dictTypeCode: String? = null,

    @Column(name = "item_code", nullable = false, length = 50, comment = "字典项编码（同一类型下唯一）")
    var itemCode: String? = null,

    @Column(name = "item_name", nullable = false, length = 100, comment = "字典项显示名称")
    var itemName: String? = null,

    @Column(name = "sort", nullable = false, comment = "排序号，越小越靠前")
    var sort: Int? = 0,

    @Column(name = "status", nullable = false, length = 20, comment = "状态：ENABLED 启用 / DISABLED 停用")
    @Convert(converter = DictItemStatusConverter::class)
    var status: DictItemStatus? = DictItemStatus.ENABLED,

    @Column(name = "remark", length = 255, comment = "备注")
    var remark: String? = null,
) : AbstractAuditableJpaEntity<DictItem, DictItem.DictItemId>() {

    /** 内嵌主键，使用雪花算法生成 */
    @Embeddable
    data class DictItemId(
        @Column(name = "id", length = 19)
        val value: Long = SnowflakeGeneratorHelper.generate()
    ) : Identifier, Serializable

    companion object {
        /** 工厂方法：根据属性创建字典项并发布创建事件 */
        fun create(attr: DictItemAttr): DictItem {
            val entity = attr.toEntity()
            entity.registerEvent(DictItemEvent.DictItemCreated(entityId = entity.id!!.value))
            return entity
        }
    }

    /** 修改字典项属性 */
    fun modify(attr: DictItemAttr) {
        attr.updateEntity(this)
        registerEvent(DictItemEvent.DictItemModified(entityId = this.id!!.value))
    }

    /** 启用 / 停用状态切换 */
    fun changeStatus(newStatus: DictItemStatus) {
        this.status = newStatus
        registerEvent(DictItemEvent.DictItemStatusChanged(entityId = this.id!!.value, status = newStatus.code))
    }
}

package com.lhl.demo.dict.domain.model

import com.lhl.common.core.helper.SnowflakeGeneratorHelper
import com.lhl.common.data.jpa.model.entity.AbstractAuditableJpaEntity
import com.lhl.demo.dict.domain.event.DictTypeEvent
import jakarta.persistence.*
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.types.Identifier
import java.io.Serializable

/**
 * 字典类型聚合根
 *
 * 表示一个数据字典的分类（如"性别"、"订单状态"），其下挂载若干字典项（DictItem）。
 * 聚合根不暴露公开构造器，统一通过 [create] 工厂方法创建。
 */
@AggregateRoot
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "dict_type", comment = "字典类型")
class DictType(
    @EmbeddedId
    override var id: DictTypeId? = null,

    @Column(name = "dict_code", nullable = false, length = 50, unique = true, comment = "字典类型编码，全局唯一")
    var dictCode: String? = null,

    @Column(name = "name", nullable = false, length = 100, comment = "字典类型名称")
    var name: String? = null,

    @Column(name = "description", length = 255, comment = "字典类型描述")
    var description: String? = null,

    @Column(name = "status", nullable = false, length = 20, comment = "状态：ENABLED 启用 / DISABLED 停用")
    @Convert(converter = DictTypeStatusConverter::class)
    var status: DictTypeStatus? = DictTypeStatus.ENABLED,
) : AbstractAuditableJpaEntity<DictType, DictType.DictTypeId>() {

    /** 内嵌主键，使用雪花算法生成 */
    @Embeddable
    data class DictTypeId(
        @Column(name = "id", length = 19)
        val value: Long = SnowflakeGeneratorHelper.generate()
    ) : Identifier, Serializable

    companion object {
        /** 工厂方法：根据属性创建字典类型并发布创建事件 */
        fun create(attr: DictTypeAttr): DictType {
            val entity = attr.toEntity()
            entity.registerEvent(DictTypeEvent.DictTypeCreated(entityId = entity.id!!.value))
            return entity
        }
    }

    /** 修改字典类型属性 */
    fun modify(attr: DictTypeAttr) {
        attr.updateEntity(this)
        registerEvent(DictTypeEvent.DictTypeModified(entityId = this.id!!.value))
    }

    /** 启用 / 停用状态切换 */
    fun changeStatus(newStatus: DictTypeStatus) {
        this.status = newStatus
        registerEvent(DictTypeEvent.DictTypeStatusChanged(entityId = this.id!!.value, status = newStatus.code))
    }
}

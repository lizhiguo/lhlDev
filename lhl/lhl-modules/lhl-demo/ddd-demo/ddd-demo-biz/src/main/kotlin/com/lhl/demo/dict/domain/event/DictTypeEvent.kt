package com.lhl.demo.dict.domain.event

import com.lhl.common.core.model.AbstractDomainEvent

/**
 * 字典类型领域事件
 *
 * 事件仅携带 entityId，消费方需要详情时可回查。由聚合根领域方法通过 [registerEvent] 发布，
 * Spring Data 在持久化时自动派发。
 */
sealed class DictTypeEvent : AbstractDomainEvent() {
    data class DictTypeCreated(val entityId: Long) : DictTypeEvent()
    data class DictTypeModified(val entityId: Long) : DictTypeEvent()
    data class DictTypeStatusChanged(val entityId: Long, val status: String) : DictTypeEvent()
    data class DictTypeDeleted(val entityId: Long) : DictTypeEvent()
}

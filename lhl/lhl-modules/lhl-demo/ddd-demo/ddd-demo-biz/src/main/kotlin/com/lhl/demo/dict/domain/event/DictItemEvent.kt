package com.lhl.demo.dict.domain.event

import com.lhl.common.core.model.AbstractDomainEvent

/**
 * 字典项领域事件
 */
sealed class DictItemEvent : AbstractDomainEvent() {
    data class DictItemCreated(val entityId: Long) : DictItemEvent()
    data class DictItemModified(val entityId: Long) : DictItemEvent()
    data class DictItemStatusChanged(val entityId: Long, val status: String) : DictItemEvent()
    data class DictItemDeleted(val entityId: Long) : DictItemEvent()
}

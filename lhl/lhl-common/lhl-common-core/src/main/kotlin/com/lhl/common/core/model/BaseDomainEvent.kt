package com.lhl.common.core.model

import java.time.Instant
import java.util.UUID

/**
 * 领域事件基础接口
 */
interface DomainEvent {
    val occurredOn: Instant
    val eventId: String
}


/**
 * 领域事件默认实现（供 sealed class 继承）
 */
abstract class AbstractDomainEvent : DomainEvent {
    override val eventId: String = UUID.randomUUID().toString()
    override val occurredOn: Instant = Instant.now()
}
package team.themoment.datagsm.common.domain.event.dto.internal

import team.themoment.datagsm.common.domain.event.entity.constant.EventType

data class EventDispatchRequested(
    val eventType: EventType,
    val data: Any,
)

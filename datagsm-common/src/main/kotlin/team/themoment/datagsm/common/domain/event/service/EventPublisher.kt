package team.themoment.datagsm.common.domain.event.service

import team.themoment.datagsm.common.domain.event.entity.constant.EventType

interface EventPublisher {
    fun dispatch(
        event: EventType,
        data: Any,
    )
}

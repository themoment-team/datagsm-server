package team.themoment.datagsm.common.domain.event.service.impl

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.payload.EventPayload
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.event.service.EventSender
import team.themoment.sdk.logging.logger.logger
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Service
class EventPublisherImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val eventSender: EventSender,
) : EventPublisher {
    private val objectMapper = JsonMapper.builder().build()

    @Async
    @Transactional(readOnly = true)
    override fun dispatch(
        event: EventType,
        data: Any,
    ) {
        runCatching {
            val targets = eventJpaRepository.findAllByEventsContainsAndIsActiveTrue(event)
            if (targets.isEmpty()) return

            val payload =
                EventPayload(
                    id = "evt_${UUID.randomUUID().toString().replace("-", "")}",
                    event = event.eventName,
                    timestamp = Instant.now().toString(),
                    data = data,
                )
            val payloadJson = objectMapper.writeValueAsString(payload)
            targets.forEach { target ->
                eventSender.send(target.targetUrl, target.secret, payloadJson)
            }
        }.onFailure { e ->
            logger().error("Failed to dispatch event {} error {}", event, e.message, e)
        }
    }
}

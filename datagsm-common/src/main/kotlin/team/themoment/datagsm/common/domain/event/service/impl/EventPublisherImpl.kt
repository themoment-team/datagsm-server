package team.themoment.datagsm.common.domain.event.service.impl

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import team.themoment.datagsm.common.domain.event.dto.payload.EventPayload
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
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

    // 재시도를 포함한 전송 구간 내내 커넥션을 점유하지 않도록 @Transactional을 두지 않는다.
    // 대상 조회는 리포지토리 자체 트랜잭션으로 처리된다.
    @Async
    override fun dispatch(
        event: EventType,
        data: Any,
    ) {
        val targets =
            eventJpaRepository.findAllByEventsContainsAndIsActiveTrueAndVerificationStatus(
                event,
                EventVerificationStatus.VERIFIED,
            )
        if (targets.isEmpty()) return

        val payload =
            EventPayload(
                id = "evt_${UUID.randomUUID().toString().replace("-", "")}",
                event = event.eventName,
                timestamp = Instant.now().toString(),
                data = data,
            )
        val payloadJson = objectMapper.writeValueAsString(payload)

        var internalFailure: Throwable? = null
        targets.forEach { target ->
            runCatching {
                eventSender.send(target.targetUrl, target.secret, payloadJson)
            }.onFailure { e ->
                if (e is RestClientException) {
                    logger().warn(
                        "Failed to deliver event {} after 3 attempts url {} error {}",
                        event.eventName,
                        target.targetUrl,
                        e.message,
                    )
                } else {
                    logger().error(
                        "Failed to deliver event {} due to an internal error url {}",
                        event.eventName,
                        target.targetUrl,
                    )
                    val firstFailure = internalFailure
                    if (firstFailure == null) internalFailure = e else firstFailure.addSuppressed(e)
                }
            }
        }

        internalFailure?.let { throw it }
    }
}

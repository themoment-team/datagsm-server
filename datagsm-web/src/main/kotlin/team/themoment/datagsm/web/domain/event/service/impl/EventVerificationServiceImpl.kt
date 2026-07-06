package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.EventVerificationService
import team.themoment.sdk.logging.logger.logger

@Service
class EventVerificationServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
) : EventVerificationService {
    @Async
    @Transactional
    override fun verifyAsync(eventId: Long) {
        val event = eventJpaRepository.findById(eventId).orElse(null) ?: return

        event.verificationStatus =
            if (EventUrlValidator.isPrivateOrLocalUrl(event.targetUrl)) {
                logger().warn("Event url verification failed for eventId {}", eventId)
                EventVerificationStatus.FAILED
            } else {
                EventVerificationStatus.VERIFIED
            }
    }
}

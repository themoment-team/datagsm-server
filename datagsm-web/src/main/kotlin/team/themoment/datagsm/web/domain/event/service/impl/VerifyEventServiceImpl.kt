package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.VerifyEventService
import team.themoment.sdk.logging.logger.logger

@Service
class VerifyEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
) : VerifyEventService {
    @Async
    @Transactional
    override fun verifyAsync(eventId: Long) {
        runCatching {
            val event =
                eventJpaRepository.findById(eventId).orElse(null)
                    ?: run {
                        logger().warn("Skipped event verification for missing eventId {}", eventId)
                        return
                    }

            event.verificationStatus =
                if (EventUrlValidator.isPrivateOrLocalUrl(event.targetUrl)) {
                    logger().warn("Failed to verify event url for eventId {}", eventId)
                    EventVerificationStatus.FAILED
                } else {
                    EventVerificationStatus.VERIFIED
                }
        }.onFailure { e ->
            logger().error("Failed to execute async event verification for eventId {}", eventId, e)
        }
    }
}

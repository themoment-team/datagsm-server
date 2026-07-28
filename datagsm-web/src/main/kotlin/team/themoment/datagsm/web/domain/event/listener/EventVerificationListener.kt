package team.themoment.datagsm.web.domain.event.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import team.themoment.datagsm.web.domain.event.dto.internal.EventVerificationRequested
import team.themoment.datagsm.web.domain.event.service.VerifyEventService

@Component
class EventVerificationListener(
    private val verifyEventService: VerifyEventService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handleVerificationRequested(event: EventVerificationRequested) {
        verifyEventService.verifyAsync(event.eventId)
    }
}

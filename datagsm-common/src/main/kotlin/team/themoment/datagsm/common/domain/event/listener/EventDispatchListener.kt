package team.themoment.datagsm.common.domain.event.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.service.EventPublisher

@Component
class EventDispatchListener(
    private val eventPublisher: EventPublisher,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handleDispatchRequested(request: EventDispatchRequested) {
        eventPublisher.dispatch(request.eventType, request.data)
    }
}

package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.EventVerificationService
import team.themoment.datagsm.web.domain.event.service.ModifyEventService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val eventVerificationService: EventVerificationService,
) : ModifyEventService {
    @Transactional
    override fun execute(
        eventId: Long,
        reqDto: ModifyEventReqDto,
    ): EventResDto {
        val account = currentUserProvider.getCurrentAccount()
        val event =
            eventJpaRepository.findByIdAndAccount(eventId, account)
                ?: throw ExpectedException("Event를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        var targetUrlChanged = false
        reqDto.targetUrl?.let {
            if (it != event.targetUrl) {
                event.targetUrl = it
                event.verificationStatus = EventVerificationStatus.PENDING
                targetUrlChanged = true
            }
        }
        reqDto.events?.let {
            event.events.clear()
            event.events.addAll(it)
        }

        if (targetUrlChanged) {
            triggerVerificationAfterCommit(event.id!!)
        }

        return EventResDto(
            id = event.id!!,
            targetUrl = event.targetUrl,
            events = event.events,
            isActive = event.isActive,
            createdAt = event.createdAt!!,
            verificationStatus = event.verificationStatus,
        )
    }

    private fun triggerVerificationAfterCommit(eventId: Long) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventVerificationService.verifyAsync(eventId)
            return
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    eventVerificationService.verifyAsync(eventId)
                }
            },
        )
    }
}

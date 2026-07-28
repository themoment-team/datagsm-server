package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.dto.internal.EventVerificationRequested
import team.themoment.datagsm.web.domain.event.service.ModifyEventService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val applicationEventPublisher: ApplicationEventPublisher,
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

        var revalidationNeeded = false
        reqDto.targetUrl?.let {
            if (it != event.targetUrl || event.verificationStatus == EventVerificationStatus.FAILED) {
                event.targetUrl = it
                event.verificationStatus = EventVerificationStatus.PENDING
                revalidationNeeded = true
            }
        }
        reqDto.events?.let {
            event.events.clear()
            event.events.addAll(it)
        }

        if (revalidationNeeded) {
            applicationEventPublisher.publishEvent(EventVerificationRequested(event.id!!))
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
}

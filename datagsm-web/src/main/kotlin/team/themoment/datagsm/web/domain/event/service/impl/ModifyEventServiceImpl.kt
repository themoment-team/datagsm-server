package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.ModifyEventService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
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

        reqDto.targetUrl?.let {
            if (EventUrlValidator.isPrivateOrLocalUrl(it)) {
                throw ExpectedException("내부 네트워크 URL은 Event 수신 URL로 등록할 수 없습니다.", HttpStatus.BAD_REQUEST)
            }
            event.targetUrl = it
        }
        reqDto.events?.let {
            event.events.clear()
            event.events.addAll(it)
        }

        return EventResDto(
            id = event.id!!,
            targetUrl = event.targetUrl,
            events = event.events,
            isActive = event.isActive,
            createdAt = event.createdAt!!,
        )
    }
}

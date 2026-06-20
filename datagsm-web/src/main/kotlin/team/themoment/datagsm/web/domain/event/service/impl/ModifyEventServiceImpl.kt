package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.persister.EventPersister
import team.themoment.datagsm.web.domain.event.service.ModifyEventService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyEventServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val eventPersister: EventPersister,
) : ModifyEventService {
    override fun execute(
        eventId: Long,
        reqDto: ModifyEventReqDto,
    ): EventResDto {
        val account = currentUserProvider.getCurrentAccount()

        reqDto.targetUrl?.let {
            if (EventUrlValidator.isPrivateOrLocalUrl(it)) {
                throw ExpectedException("내부 네트워크 URL은 Event 수신 URL로 등록할 수 없습니다.", HttpStatus.BAD_REQUEST)
            }
        }

        return eventPersister.persistModify(account, eventId, reqDto)
    }
}

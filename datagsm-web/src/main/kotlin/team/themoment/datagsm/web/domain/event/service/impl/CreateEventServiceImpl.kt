package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.CreateEventResDto
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import team.themoment.datagsm.web.domain.event.service.CreateEventService
import team.themoment.datagsm.web.domain.event.service.PersistEventService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.security.SecureRandom

@Service
class CreateEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val persistEventService: PersistEventService,
) : CreateEventService {
    override fun execute(reqDto: CreateEventReqDto): CreateEventResDto {
        val account = currentUserProvider.getCurrentAccount()

        if (eventJpaRepository.countByAccount(account) >= MAX_EVENTS_PER_ACCOUNT) {
            throw ExpectedException("Event는 최대 10개까지 등록할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        if (EventUrlValidator.isPrivateOrLocalUrl(reqDto.targetUrl)) {
            throw ExpectedException("내부 네트워크 URL은 Event 수신 URL로 등록할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        val secret = generateSecret()
        return persistEventService.persistCreate(account, reqDto, secret)
    }

    private fun generateSecret(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val MAX_EVENTS_PER_ACCOUNT = 10
        private val secureRandom = SecureRandom()
    }
}

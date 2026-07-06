package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.CreateEventResDto
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.CreateEventService
import team.themoment.datagsm.web.domain.event.service.EventVerificationService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.security.SecureRandom

@Service
class CreateEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val eventVerificationService: EventVerificationService,
) : CreateEventService {
    @Transactional
    override fun execute(reqDto: CreateEventReqDto): CreateEventResDto {
        val account = currentUserProvider.getCurrentAccount()

        if (eventJpaRepository.countByAccount(account) >= MAX_EVENTS_PER_ACCOUNT) {
            throw ExpectedException("Event는 최대 10개까지 등록할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        val secret = generateSecret()
        val event =
            EventJpaEntity().apply {
                targetUrl = reqDto.targetUrl
                events = reqDto.events.toMutableSet()
                this.account = account
                this.secret = secret
            }
        val saved = eventJpaRepository.save(event)

        triggerVerificationAfterCommit(saved.id!!)

        return CreateEventResDto(
            id = saved.id!!,
            targetUrl = saved.targetUrl,
            events = saved.events,
            isActive = saved.isActive,
            createdAt = saved.createdAt!!,
            verificationStatus = saved.verificationStatus,
            secret = secret,
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

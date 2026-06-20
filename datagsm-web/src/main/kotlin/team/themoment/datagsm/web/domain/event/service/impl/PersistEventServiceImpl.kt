package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.CreateEventResDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.entity.EventJpaEntity
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.PersistEventService
import team.themoment.sdk.exception.ExpectedException

@Service
class PersistEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
) : PersistEventService {
    @Transactional
    override fun persistCreate(
        account: AccountJpaEntity,
        reqDto: CreateEventReqDto,
        secret: String,
    ): CreateEventResDto {
        if (eventJpaRepository.countByAccount(account) >= MAX_EVENTS_PER_ACCOUNT) {
            throw ExpectedException("Event는 최대 10개까지 등록할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        val event =
            EventJpaEntity().apply {
                targetUrl = reqDto.targetUrl
                events = reqDto.events.toMutableSet()
                this.account = account
                this.secret = secret
            }
        val saved = eventJpaRepository.save(event)

        return CreateEventResDto(
            id = saved.id!!,
            targetUrl = saved.targetUrl,
            events = saved.events,
            isActive = saved.isActive,
            createdAt = saved.createdAt!!,
            secret = secret,
        )
    }

    @Transactional
    override fun persistModify(
        account: AccountJpaEntity,
        eventId: Long,
        reqDto: ModifyEventReqDto,
    ): EventResDto {
        val event =
            eventJpaRepository.findByIdAndAccount(eventId, account)
                ?: throw ExpectedException("Event를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        reqDto.targetUrl?.let { event.targetUrl = it }
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

    companion object {
        private const val MAX_EVENTS_PER_ACCOUNT = 10
    }
}

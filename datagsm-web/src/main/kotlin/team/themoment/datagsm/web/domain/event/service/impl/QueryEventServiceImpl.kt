package team.themoment.datagsm.web.domain.event.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.response.EventListResDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto
import team.themoment.datagsm.common.domain.event.repository.EventJpaRepository
import team.themoment.datagsm.web.domain.event.service.QueryEventService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class QueryEventServiceImpl(
    private val eventJpaRepository: EventJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryEventService {
    @Transactional(readOnly = true)
    override fun execute(): EventListResDto {
        val account = currentUserProvider.getCurrentAccount()
        val events =
            eventJpaRepository
                .findAllByAccount(account)
                .map { event ->
                    EventResDto(
                        id = event.id!!,
                        targetUrl = event.targetUrl,
                        events = event.events,
                        isActive = event.isActive,
                        createdAt = event.createdAt!!,
                    )
                }
        return EventListResDto(events = events)
    }
}

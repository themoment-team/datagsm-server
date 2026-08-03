package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.mapper.EventObjectMapper
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMyGithubIdReqDto
import team.themoment.datagsm.web.domain.student.service.ModifyMyGithubIdService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class ModifyMyGithubIdServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ModifyMyGithubIdService {
    @Transactional
    override fun execute(reqDto: UpdateMyGithubIdReqDto) {
        val student = currentUserProvider.getCurrentStudent()

        val oldObj = EventObjectMapper.from(student)

        student.githubId = reqDto.githubId

        val newObj = EventObjectMapper.from(student)
        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.STUDENT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, newObj)),
                ),
            ),
        )
    }
}

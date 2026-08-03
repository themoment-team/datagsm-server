package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.mapper.EventObjectMapper
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.DeleteProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DeleteProjectService {
    @Transactional
    override fun execute(projectId: Long) {
        val project =
            projectJpaRepository
                .findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val oldObj = EventObjectMapper.from(project)

        projectJpaRepository.delete(project)

        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.PROJECT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, EmptyEventObject())),
                ),
            ),
        )
    }
}

package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectDeletedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.service.DeleteProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val eventPublisher: EventPublisher,
) : DeleteProjectService {
    @Transactional
    override fun execute(projectId: Long) {
        val project =
            projectJpaRepository
                .findById(projectId)
                .orElseThrow { ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
        projectJpaRepository.delete(project)

        eventPublisher.dispatch(
            EventType.PROJECT_DELETED,
            ProjectDeletedData(projectId = project.id!!, name = project.name),
        )
    }
}

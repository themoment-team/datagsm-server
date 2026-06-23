package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
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

        val oldObj = generateProjectEventObject(project)
        projectJpaRepository.delete(project)

        eventPublisher.dispatch(
            EventType.PROJECT_CHANGED,
            EventChangedData(
                old = listOf(EventChangeItem(0, oldObj)),
                new = listOf(EventChangeItem(0, EmptyEventObject())),
            ),
        )
    }

    private fun generateProjectEventObject(project: ProjectJpaEntity): ProjectEventObject =
        ProjectEventObject(
            projectId = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status.name,
            club = project.club?.let { EventClubRef(it.id!!, it.name) },
            participants =
                project.participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
        )
}

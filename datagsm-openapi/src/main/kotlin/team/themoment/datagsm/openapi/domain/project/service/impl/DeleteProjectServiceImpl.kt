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
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.DeleteProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DeleteProjectService {
    @Transactional
    override fun execute(projectId: Long) {
        val project =
            projectJpaRepository
                .findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        val oldObj = generateProjectEventObject(project)

        // 이 프로젝트를 참조하는 신청 이력을 먼저 지우지 않으면 외래 키 제약에 걸린다
        projectEditRequestJpaRepository.deleteAllByOriginalProjectId(projectId)
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

    private fun generateProjectEventObject(project: ProjectJpaEntity): ProjectEventObject =
        ProjectEventObject(
            projectId = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status.name,
            deploymentUrl = project.deploymentUrl,
            club = project.club?.let { EventClubRef(it.id!!, it.name) },
            participants =
                project.participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
            repositories = project.repositories.toList(),
            techStacks = project.techStacks.toList(),
        )
}

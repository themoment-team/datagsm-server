package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.project.dto.request.EndProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.service.EndProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class EndProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : EndProjectService {
    @Transactional
    override fun execute(
        projectId: Long,
        reqDto: EndProjectReqDto,
    ) {
        val project =
            projectJpaRepository.findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (reqDto.endYear < project.startYear) {
            throw ExpectedException("종료 연도는 시작 연도보다 크거나 같아야 합니다.", HttpStatus.BAD_REQUEST)
        }

        val oldObj = generateProjectEventObject(project)

        project.status = ProjectStatus.ENDED
        project.endYear = reqDto.endYear

        val newObj = generateProjectEventObject(project)
        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.PROJECT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, newObj)),
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
            club = project.club?.let { EventClubRef(it.id!!, it.name) },
            participants =
                project.participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
        )
}

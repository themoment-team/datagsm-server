package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.AcceptProjectRequestService
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

@Service
class AcceptProjectRequestServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
    private val projectEditRequestMapper: ProjectEditRequestMapper,
    private val projectIconStorage: ProjectIconStorage,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : AcceptProjectRequestService {
    @Transactional
    override fun execute(requestId: Long): ProjectResDto {
        val request =
            projectEditRequestJpaRepository
                .findById(requestId)
                .orElseThrow { ExpectedException("신청 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        if (request.requestStatus != ProjectRequestStatus.PENDING) {
            throw ExpectedException("이미 처리된 신청입니다.", HttpStatus.CONFLICT)
        }

        val originalProject = request.originalProject
        val isNewProject = originalProject == null

        if (isNewProject && projectJpaRepository.existsByName(request.name)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다.", HttpStatus.CONFLICT)
        }
        if (!isNewProject && projectJpaRepository.existsByNameAndIdNot(request.name, originalProject.id!!)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다.", HttpStatus.CONFLICT)
        }

        val oldObj: Any =
            if (isNewProject) EmptyEventObject() else projectEditRequestMapper.toEventObject(originalProject)

        val project = originalProject ?: ProjectJpaEntity().apply { status = ProjectStatus.ACTIVE }
        applySnapshot(project, request)

        val savedProject = projectJpaRepository.save(project)

        request.requestStatus = ProjectRequestStatus.ACCEPTED
        request.processedAt = LocalDateTime.now()
        projectEditRequestJpaRepository.save(request)

        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.PROJECT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, projectEditRequestMapper.toEventObject(savedProject))),
                ),
            ),
        )

        return toProjectResDto(savedProject)
    }

    private fun applySnapshot(
        project: ProjectJpaEntity,
        request: ProjectEditRequestJpaEntity,
    ) {
        project.name = request.name
        project.description = request.description
        project.startYear = request.startYear
        project.club = request.club
        project.participants = request.participants.toMutableSet()
        project.repositories = request.repositories.toMutableSet()
        project.techStacks = request.techStacks.toMutableSet()
        project.iconKey = request.iconKey
        project.deploymentUrl = request.deploymentUrl
        if (project.appliedBy == null) {
            project.appliedBy = request.requestedBy
        }
    }

    private fun toProjectResDto(project: ProjectJpaEntity): ProjectResDto =
        ProjectResDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status,
            iconUrl = projectIconStorage.toIconUrl(project.iconKey),
            deploymentUrl = project.deploymentUrl,
            club = project.club?.let { projectEditRequestMapper.toClubSummary(it) },
            participants = project.participants.map { projectEditRequestMapper.toParticipantInfo(it) },
            repositories = project.repositories.toList(),
            techStacks = project.techStacks.toList(),
        )
}

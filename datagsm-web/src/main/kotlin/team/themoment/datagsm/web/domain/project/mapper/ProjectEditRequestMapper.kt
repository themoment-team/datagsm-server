package team.themoment.datagsm.web.domain.project.mapper

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.web.global.storage.ProjectIconStorage

@Component
class ProjectEditRequestMapper(
    private val projectIconStorage: ProjectIconStorage,
) {
    fun toResDto(request: ProjectEditRequestJpaEntity): ProjectEditRequestResDto =
        ProjectEditRequestResDto(
            id = request.id!!,
            originalProjectId = request.originalProject?.id,
            requestedBy = toParticipantInfo(request.requestedBy),
            name = request.name,
            description = request.description,
            startYear = request.startYear,
            iconUrl = projectIconStorage.toIconUrl(request.iconKey),
            deploymentUrl = request.deploymentUrl,
            club = request.club?.let { toClubSummary(it) },
            participants = request.participants.map { toParticipantInfo(it) },
            repositories = request.repositories.toList(),
            techStacks = request.techStacks.toList(),
            requestStatus = request.requestStatus,
            rejectReason = request.rejectReason,
            requestedAt = request.requestedAt,
            processedAt = request.processedAt,
        )

    fun toParticipantInfo(student: StudentJpaEntity): ParticipantInfoDto =
        ParticipantInfoDto(
            id = student.id!!,
            name = student.name,
            email = student.email,
            studentNumber = student.studentNumber?.fullStudentNumber,
            major = student.major,
            sex = student.sex,
        )

    fun toClubSummary(club: ClubJpaEntity): ClubSummaryDto = ClubSummaryDto(id = club.id!!, name = club.name, type = club.type)

    fun toEventObject(project: ProjectJpaEntity): ProjectEventObject =
        ProjectEventObject(
            projectId = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status.name,
            deploymentUrl = project.deploymentUrl,
            club = project.club?.let { EventClubRef(it.id!!, it.name) },
            participants = project.participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
            repositories = project.repositories.toList(),
            techStacks = project.techStacks.toList(),
        )
}

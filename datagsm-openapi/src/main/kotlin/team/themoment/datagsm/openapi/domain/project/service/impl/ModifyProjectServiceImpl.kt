package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.ModifyProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ModifyProjectService {
    @Transactional
    override fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto {
        val project =
            projectJpaRepository
                .findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (projectJpaRepository.existsByNameAndIdNot(reqDto.name, projectId)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다.", HttpStatus.CONFLICT)
        }
        val ownerClub =
            reqDto.clubId?.let { clubId ->
                clubJpaRepository.findByIdOrNull(clubId)
                    ?: throw ExpectedException(
                        "동아리를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND,
                    )
            }

        val newParticipants =
            if (reqDto.participantIds.isNotEmpty()) {
                val foundStudents = studentJpaRepository.findAllById(reqDto.participantIds).toMutableSet()
                val foundIds = foundStudents.map { it.id }.toSet()
                val notFoundIds = reqDto.participantIds.filterNot { it in foundIds }

                if (notFoundIds.isNotEmpty()) {
                    throw ExpectedException(
                        "해당 학생 데이터를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND,
                    )
                }
                foundStudents
            } else {
                mutableSetOf()
            }

        val oldObj = generateProjectEventObject(project)

        project.name = reqDto.name
        project.description = reqDto.description
        project.startYear = reqDto.startYear
        project.club = ownerClub
        project.participants = newParticipants

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

        return ProjectResDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status,
            club = project.club?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            participants =
                project.participants.map { student ->
                    ParticipantInfoDto(
                        id = student.id!!,
                        name = student.name,
                        email = student.email,
                        studentNumber = student.studentNumber?.fullStudentNumber,
                        major = student.major,
                        sex = student.sex,
                    )
                },
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

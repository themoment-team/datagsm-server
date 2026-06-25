package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.event.dto.payload.EmptyEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangeItem
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.service.EventPublisher
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.project.service.CreateProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val eventPublisher: EventPublisher,
) : CreateProjectService {
    @Transactional
    override fun execute(projectReqDto: ProjectReqDto): ProjectResDto {
        if (projectJpaRepository.existsByName(projectReqDto.name)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다.", HttpStatus.CONFLICT)
        }

        val ownerClub =
            projectReqDto.clubId?.let { clubId ->
                clubJpaRepository
                    .findById(clubId)
                    .orElseThrow {
                        ExpectedException(
                            "동아리를 찾을 수 없습니다.",
                            HttpStatus.NOT_FOUND,
                        )
                    }
            }

        val participants =
            if (projectReqDto.participantIds.isNotEmpty()) {
                val foundStudents = studentJpaRepository.findAllById(projectReqDto.participantIds).toMutableSet()
                val foundIds = foundStudents.map { it.id }.toSet()
                val notFoundIds = projectReqDto.participantIds.filterNot { it in foundIds }
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

        if (projectReqDto.status == ProjectStatus.ENDED) {
            val endYear =
                projectReqDto.endYear
                    ?: throw ExpectedException("종료 연도를 입력해주세요.", HttpStatus.BAD_REQUEST)
            if (endYear < projectReqDto.startYear) {
                throw ExpectedException("종료 연도는 시작 연도보다 크거나 같아야 합니다.", HttpStatus.BAD_REQUEST)
            }
        }

        val projectEntity =
            ProjectJpaEntity().apply {
                name = projectReqDto.name
                description = projectReqDto.description
                startYear = projectReqDto.startYear
                status = projectReqDto.status
                endYear = projectReqDto.endYear
                this.club = ownerClub
                this.participants = participants
            }
        val savedProjectEntity = projectJpaRepository.save(projectEntity)

        val newObj = generateProjectEventObject(savedProjectEntity)
        eventPublisher.dispatch(
            EventType.PROJECT_UPDATED,
            EventChangedData(
                old = listOf(EventChangeItem(0, EmptyEventObject())),
                new = listOf(EventChangeItem(0, newObj)),
            ),
        )

        return ProjectResDto(
            id = savedProjectEntity.id!!,
            name = savedProjectEntity.name,
            description = savedProjectEntity.description,
            startYear = savedProjectEntity.startYear,
            endYear = savedProjectEntity.endYear,
            status = savedProjectEntity.status,
            club = ownerClub?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            participants =
                savedProjectEntity.participants.map { student ->
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

package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.CreateProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : CreateProjectService {
    @Transactional
    override fun execute(projectReqDto: ProjectReqDto): ProjectResDto {
        if (projectJpaRepository.existsByName(projectReqDto.name)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다.", HttpStatus.CONFLICT)
        }

        val ownerClub =
            projectReqDto.clubId?.let { clubId ->
                clubJpaRepository.findByIdOrNull(clubId)
                    ?: throw ExpectedException(
                        "동아리를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND,
                    )
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
}

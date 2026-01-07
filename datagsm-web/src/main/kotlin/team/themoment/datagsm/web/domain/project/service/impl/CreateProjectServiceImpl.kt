package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.ProjectJpaEntity
import team.themoment.datagsm.web.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.web.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.web.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.web.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.service.CreateProjectService
import team.themoment.datagsm.web.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.web.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.global.exception.error.ExpectedException

@Service
@Transactional
class CreateProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : CreateProjectService {
    override fun execute(projectReqDto: ProjectReqDto): ProjectResDto {
        if (projectJpaRepository.existsByName(projectReqDto.name)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${projectReqDto.name}", HttpStatus.CONFLICT)
        }

        val ownerClub =
            clubJpaRepository
                .findById(projectReqDto.clubId)
                .orElseThrow {
                    ExpectedException(
                        "동아리를 찾을 수 없습니다. clubId: ${projectReqDto.clubId}",
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
                        "${notFoundIds.joinToString(", ")} 에 대응하는 학생 데이터를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND,
                    )
                }
                foundStudents
            } else {
                mutableSetOf()
            }

        val projectEntity =
            ProjectJpaEntity().apply {
                name = projectReqDto.name
                description = projectReqDto.description
                this.club = ownerClub
                this.participants = participants
            }
        val savedProjectEntity = projectJpaRepository.save(projectEntity)

        return ProjectResDto(
            id = savedProjectEntity.id!!,
            name = savedProjectEntity.name,
            description = savedProjectEntity.description,
            club =
                ClubSummaryDto(
                    id = ownerClub.id!!,
                    name = ownerClub.name,
                    type = ownerClub.type,
                ),
            participants =
                savedProjectEntity.participants.map { student ->
                    ParticipantInfoDto(
                        id = student.id!!,
                        name = student.name,
                        email = student.email,
                        studentNumber = student.studentNumber.fullStudentNumber,
                        major = student.major,
                        sex = student.sex,
                    )
                },
        )
    }
}

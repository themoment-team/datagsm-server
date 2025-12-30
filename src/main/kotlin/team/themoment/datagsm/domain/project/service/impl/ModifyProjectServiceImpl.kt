package team.themoment.datagsm.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.ModifyProjectService
import team.themoment.datagsm.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class ModifyProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyProjectService {
    override fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto {
        val project =
            projectJpaRepository
                .findById(projectId)
                .orElseThrow { ExpectedException("프로젝트를 찾을 수 없습니다. projectId: $projectId", HttpStatus.NOT_FOUND) }
        if (projectJpaRepository.existsByNameAndIdNot(reqDto.name, projectId)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
        }
        val ownerClub =
            clubJpaRepository
                .findById(reqDto.clubId)
                .orElseThrow {
                    ExpectedException(
                        "동아리를 찾을 수 없습니다. clubId: ${reqDto.clubId}",
                        HttpStatus.NOT_FOUND,
                    )
                }

        val newParticipants =
            if (reqDto.participantIds.isNotEmpty()) {
                studentJpaRepository.findAllById(reqDto.participantIds).toMutableSet()
            } else {
                mutableSetOf()
            }

        if (newParticipants.size != reqDto.participantIds.size) {
            throw ExpectedException(
                "일부 학생을 찾을 수 없습니다. 요청한 학생 수: ${reqDto.participantIds.size}, 찾은 학생 수: ${newParticipants.size}",
                HttpStatus.NOT_FOUND,
            )
        }

        project.name = reqDto.name
        project.description = reqDto.description
        project.club = ownerClub
        project.participants.clear()
        project.participants.addAll(newParticipants)

        return ProjectResDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            club = project.club?.let { ClubSummaryDto(id = it.id!!, name = it.name, type = it.type) },
            participants =
                project.participants.map { student ->
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

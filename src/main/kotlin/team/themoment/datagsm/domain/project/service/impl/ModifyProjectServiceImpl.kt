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
                val foundStudents = studentJpaRepository.findAllById(reqDto.participantIds).toMutableSet()
                val foundIds = foundStudents.map { it.id }.toSet()
                val notFoundIds = reqDto.participantIds.filterNot { it in foundIds }

                if (notFoundIds.isNotEmpty()) {
                    throw ExpectedException(
                        "존재하지 않는 학생 ID: ${notFoundIds.joinToString(", ")}",
                        HttpStatus.NOT_FOUND,
                    )
                }
                foundStudents
            } else {
                mutableSetOf()
            }

        project.name = reqDto.name
        project.description = reqDto.description
        project.club = ownerClub
        project.participants = newParticipants

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

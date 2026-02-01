package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.BaseStudent
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.ModifyProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyProjectService {
    @Transactional
    override fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto {
        val project =
            projectJpaRepository
                .findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다. projectId: $projectId", HttpStatus.NOT_FOUND)
        if (projectJpaRepository.existsByNameAndIdNot(reqDto.name, projectId)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
        }
        val ownerClub =
            clubJpaRepository
                .findByIdOrNull(reqDto.clubId)
                ?: throw ExpectedException(
                    "동아리를 찾을 수 없습니다. clubId: ${reqDto.clubId}",
                    HttpStatus.NOT_FOUND,
                )

        val newParticipants: MutableSet<BaseStudent> =
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
                        studentNumber = (student as? EnrolledStudent)?.studentNumber?.fullStudentNumber,
                        major = (student as? EnrolledStudent)?.major,
                        sex = student.sex,
                    )
                },
        )
    }
}

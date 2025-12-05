package team.themoment.datagsm.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.ModifyProjectService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class ModifyProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
) : ModifyProjectService {
    override fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto {
        val project =
            projectJpaRepository
                .findById(projectId)
                .orElseThrow { ExpectedException("프로젝트를 찾을 수 없습니다. projectId: $projectId", HttpStatus.NOT_FOUND) }

        if (reqDto.name != project.name) {
            if (projectJpaRepository.existsByNameAndIdNot(reqDto.name, projectId)) {
                throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
            }
            project.name = reqDto.name
        }

        project.description = reqDto.description

        if (reqDto.clubId != project.club?.id) {
            val ownerClub =
                clubJpaRepository
                    .findById(reqDto.clubId)
                    .orElseThrow {
                        ExpectedException(
                            "동아리를 찾을 수 없습니다. clubId: ${reqDto.clubId}",
                            HttpStatus.NOT_FOUND,
                        )
                    }
            project.club = ownerClub
        }

        return ProjectResDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            club = project.club?.let { ClubResDto(id = it.id!!, name = it.name, type = it.type) },
        )
    }
}

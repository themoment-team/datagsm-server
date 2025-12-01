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

        if (reqDto.projectName != project.name) {
            if (projectJpaRepository.existsByProjectNameAndProjectIdNot(reqDto.projectName, projectId)) {
                throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${reqDto.projectName}", HttpStatus.CONFLICT)
            }
            project.name = reqDto.projectName
        }

        project.description = reqDto.projectDescription

        if (reqDto.projectOwnerClubId != project.ownerClub.id) {
            val ownerClub =
                clubJpaRepository
                    .findById(reqDto.projectOwnerClubId)
                    .orElseThrow {
                        ExpectedException(
                            "동아리를 찾을 수 없습니다. clubId: ${reqDto.projectOwnerClubId}",
                            HttpStatus.NOT_FOUND,
                        )
                    }
            project.ownerClub = ownerClub
        }

        return ProjectResDto(
            projectId = project.id!!,
            projectName = project.name,
            projectDescription = project.description,
            projectOwnerClub =
                ClubResDto(
                    clubId = project.ownerClub.id!!,
                    clubName = project.ownerClub.name,
                    clubType = project.ownerClub.type,
                ),
        )
    }
}

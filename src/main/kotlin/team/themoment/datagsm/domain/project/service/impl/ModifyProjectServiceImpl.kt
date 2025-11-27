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

        if (reqDto.projectName != project.projectName) {
            if (projectJpaRepository.existsByProjectNameAndProjectIdNot(reqDto.projectName, projectId)) {
                throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${reqDto.projectName}", HttpStatus.CONFLICT)
            }
            project.projectName = reqDto.projectName
        }

        project.projectDescription = reqDto.projectDescription

        if (reqDto.projectOwnerClubId != project.projectOwnerClub.clubId) {
            val ownerClub =
                clubJpaRepository
                    .findById(reqDto.projectOwnerClubId)
                    .orElseThrow {
                        ExpectedException(
                            "동아리를 찾을 수 없습니다. clubId: ${reqDto.projectOwnerClubId}",
                            HttpStatus.NOT_FOUND,
                        )
                    }
            project.projectOwnerClub = ownerClub
        }

        return ProjectResDto(
            projectId = project.projectId!!,
            projectName = project.projectName,
            projectDescription = project.projectDescription,
            projectOwnerClub =
                ClubResDto(
                    clubId = project.projectOwnerClub.clubId!!,
                    clubName = project.projectOwnerClub.clubName,
                    clubType = project.projectOwnerClub.clubType,
                ),
        )
    }
}

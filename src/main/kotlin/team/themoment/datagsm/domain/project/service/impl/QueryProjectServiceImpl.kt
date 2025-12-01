package team.themoment.datagsm.domain.project.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.QueryProjectService

@Service
@Transactional(readOnly = true)
class QueryProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
) : QueryProjectService {
    override fun execute(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        page: Int,
        size: Int,
    ): ProjectListResDto {
        val projectPage =
            projectJpaRepository.searchProjectWithPaging(
                id = projectId,
                name = projectName,
                clubId = clubId,
                pageable = PageRequest.of(page, size),
            )

        return ProjectListResDto(
            totalPages = projectPage.totalPages,
            totalElements = projectPage.totalElements,
            projects =
                projectPage.content.map { entity ->
                    ProjectResDto(
                        projectId = entity.id!!,
                        projectName = entity.name,
                        projectDescription = entity.description,
                        projectOwnerClub =
                            ClubResDto(
                                clubId = entity.ownerClub.id!!,
                                clubName = entity.ownerClub.name,
                                clubType = entity.ownerClub.type,
                            ),
                    )
                },
        )
    }
}

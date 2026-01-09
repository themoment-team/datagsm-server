package team.themoment.datagsm.resource.domain.project.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.dto.club.internal.ClubSummaryDto
import team.themoment.datagsm.common.dto.project.response.ProjectListResDto
import team.themoment.datagsm.common.dto.project.response.ProjectResDto
import team.themoment.datagsm.common.dto.student.internal.ParticipantInfoDto
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.project.service.QueryProjectService

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
        sortBy: ProjectSortBy?,
        sortDirection: SortDirection,
    ): ProjectListResDto {
        val projectPage =
            projectJpaRepository.searchProjectWithPaging(
                id = projectId,
                name = projectName,
                clubId = clubId,
                pageable = PageRequest.of(page, size),
                sortBy = sortBy,
                sortDirection = sortDirection,
            )

        return ProjectListResDto(
            totalPages = projectPage.totalPages,
            totalElements = projectPage.totalElements,
            projects =
                projectPage.content.map { project ->
                    ProjectResDto(
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
                },
        )
    }
}

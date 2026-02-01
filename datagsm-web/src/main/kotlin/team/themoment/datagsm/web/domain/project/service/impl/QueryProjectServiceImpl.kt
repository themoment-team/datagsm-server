package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectSortBy
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.web.domain.project.service.QueryProjectService

@Service
class QueryProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
) : QueryProjectService {
    @Transactional(readOnly = true)
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
                                    studentNumber = (student as? EnrolledStudent)?.studentNumber?.fullStudentNumber,
                                    major = (student as? EnrolledStudent)?.major,
                                    sex = student.sex,
                                )
                            },
                    )
                },
        )
    }
}

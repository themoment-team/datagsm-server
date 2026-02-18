package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.openapi.domain.project.service.QueryProjectService

@Service
@Transactional(readOnly = true)
class QueryProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
) : QueryProjectService {
    override fun execute(queryReq: QueryProjectReqDto): ProjectListResDto {
        val projectPage =
            projectJpaRepository.searchProjectWithPaging(
                id = queryReq.projectId,
                name = queryReq.projectName,
                clubId = queryReq.clubId,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
                sortBy = queryReq.sortBy,
                sortDirection = queryReq.sortDirection,
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
                                    studentNumber = student.studentNumber?.fullStudentNumber,
                                    major = student.major,
                                    sex = student.sex,
                                )
                            },
                    )
                },
        )
    }
}

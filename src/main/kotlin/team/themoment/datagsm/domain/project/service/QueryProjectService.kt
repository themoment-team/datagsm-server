package team.themoment.datagsm.domain.project.service

import team.themoment.datagsm.domain.project.dto.response.ProjectListResDto

interface QueryProjectService {
    fun execute(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        page: Int,
        size: Int,
    ): ProjectListResDto
}

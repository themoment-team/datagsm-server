package team.themoment.datagsm.resource.domain.project.service

import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.project.dto.response.ProjectListResDto

interface QueryProjectService {
    fun execute(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        page: Int,
        size: Int,
        sortBy: ProjectSortBy? = null,
        sortDirection: SortDirection = SortDirection.ASC,
    ): ProjectListResDto
}

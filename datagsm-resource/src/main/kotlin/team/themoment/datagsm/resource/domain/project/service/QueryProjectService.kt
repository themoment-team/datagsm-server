package team.themoment.datagsm.resource.domain.project.service

import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.common.dto.project.response.ProjectListResDto
import team.themoment.datagsm.common.global.constant.SortDirection

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

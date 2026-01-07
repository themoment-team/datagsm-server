package team.themoment.datagsm.domain.project.service

import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.domain.project.dto.response.ProjectListResDto
import team.themoment.datagsm.global.common.constant.SortDirection

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

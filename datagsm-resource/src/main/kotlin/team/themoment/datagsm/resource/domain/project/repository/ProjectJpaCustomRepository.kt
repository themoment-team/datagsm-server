package team.themoment.datagsm.resource.domain.project.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.project.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.resource.global.common.constant.SortDirection

interface ProjectJpaCustomRepository {
    fun searchProjectWithPaging(
        id: Long?,
        name: String?,
        clubId: Long?,
        pageable: Pageable,
        sortBy: ProjectSortBy?,
        sortDirection: SortDirection,
    ): Page<ProjectJpaEntity>
}

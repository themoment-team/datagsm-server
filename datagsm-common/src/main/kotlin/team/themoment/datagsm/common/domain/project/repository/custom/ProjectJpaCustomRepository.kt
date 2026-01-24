package team.themoment.datagsm.common.domain.project.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectSortBy
import team.themoment.datagsm.common.global.constant.SortDirection

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

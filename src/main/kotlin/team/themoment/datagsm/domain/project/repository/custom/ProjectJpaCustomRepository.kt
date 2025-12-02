package team.themoment.datagsm.domain.project.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity

interface ProjectJpaCustomRepository {
    fun searchProjectWithPaging(
        id: Long?,
        name: String?,
        clubId: Long?,
        pageable: Pageable,
    ): Page<ProjectJpaEntity>
}

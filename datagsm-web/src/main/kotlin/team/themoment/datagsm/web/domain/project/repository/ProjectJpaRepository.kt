package team.themoment.datagsm.web.domain.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.project.ProjectJpaEntity
import team.themoment.datagsm.web.domain.project.repository.custom.ProjectJpaCustomRepository

interface ProjectJpaRepository :
    JpaRepository<ProjectJpaEntity, Long>,
    ProjectJpaCustomRepository {
    fun existsByName(projectName: String): Boolean

    fun existsByNameAndIdNot(
        projectName: String,
        projectId: Long,
    ): Boolean
}

    package team.themoment.datagsm.domain.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.domain.project.repository.custom.ProjectJpaCustomRepository

interface ProjectJpaRepository :
    JpaRepository<ProjectJpaEntity, Long>,
    ProjectJpaCustomRepository {
    fun existsByProjectName(projectName: String): Boolean

    fun existsByProjectNameAndProjectIdNot(
        projectName: String,
        projectId: Long,
    ): Boolean
}

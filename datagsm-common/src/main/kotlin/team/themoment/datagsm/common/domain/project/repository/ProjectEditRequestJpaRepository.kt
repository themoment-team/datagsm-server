package team.themoment.datagsm.common.domain.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.custom.ProjectEditRequestJpaCustomRepository
import java.util.Optional

interface ProjectEditRequestJpaRepository :
    JpaRepository<ProjectEditRequestJpaEntity, Long>,
    ProjectEditRequestJpaCustomRepository {
    fun findByOriginalProjectIdAndRequestStatus(
        originalProjectId: Long,
        requestStatus: ProjectRequestStatus,
    ): Optional<ProjectEditRequestJpaEntity>

    fun findAllByOriginalProjectIdInAndRequestStatus(
        originalProjectIds: Collection<Long>,
        requestStatus: ProjectRequestStatus,
    ): List<ProjectEditRequestJpaEntity>

    fun deleteAllByOriginalProjectId(originalProjectId: Long)
}

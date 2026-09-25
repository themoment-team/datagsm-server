package team.themoment.datagsm.common.domain.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.custom.ProjectEditRequestJpaCustomRepository
import java.util.Optional

interface ProjectEditRequestJpaRepository :
    JpaRepository<ProjectEditRequestJpaEntity, Long>,
    ProjectEditRequestJpaCustomRepository {
    /** 프로젝트당 신청 행을 하나만 유지하므로 상태와 무관하게 단건으로 조회한다 */
    fun findByOriginalProjectId(originalProjectId: Long): Optional<ProjectEditRequestJpaEntity>

    fun findAllByOriginalProjectIdInAndRequestStatus(
        originalProjectIds: Collection<Long>,
        requestStatus: ProjectRequestStatus,
    ): List<ProjectEditRequestJpaEntity>

    fun deleteAllByOriginalProjectId(originalProjectId: Long)
}

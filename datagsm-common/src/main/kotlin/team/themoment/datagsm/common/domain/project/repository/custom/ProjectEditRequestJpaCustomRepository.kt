package team.themoment.datagsm.common.domain.project.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus

interface ProjectEditRequestJpaCustomRepository {
    fun searchEditRequestWithPaging(
        requestStatus: ProjectRequestStatus?,
        requestedById: Long?,
        pageable: Pageable,
    ): Page<ProjectEditRequestJpaEntity>

    fun findAllByParticipantOrRequester(studentId: Long): List<ProjectEditRequestJpaEntity>
}

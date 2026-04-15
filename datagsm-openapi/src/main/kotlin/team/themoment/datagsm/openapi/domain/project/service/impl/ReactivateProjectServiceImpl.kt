package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.ReactivateProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class ReactivateProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
) : ReactivateProjectService {
    @Transactional
    override fun execute(projectId: Long) {
        val project =
            projectJpaRepository.findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        project.status = ProjectStatus.ACTIVE
        project.endYear = null
    }
}

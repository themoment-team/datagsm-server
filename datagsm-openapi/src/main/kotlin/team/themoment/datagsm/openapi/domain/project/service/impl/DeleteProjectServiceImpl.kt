package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.DeleteProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
) : DeleteProjectService {
    @Transactional
    override fun execute(projectId: Long) {
        val project =
            projectJpaRepository
                .findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다. projectId: $projectId", HttpStatus.NOT_FOUND)
        projectJpaRepository.delete(project)
    }
}

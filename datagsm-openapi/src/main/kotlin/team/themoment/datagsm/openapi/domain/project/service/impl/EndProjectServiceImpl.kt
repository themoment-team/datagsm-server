package team.themoment.datagsm.openapi.domain.project.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.request.EndProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.openapi.domain.project.service.EndProjectService
import team.themoment.sdk.exception.ExpectedException

@Service
class EndProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
) : EndProjectService {
    @Transactional
    override fun execute(
        projectId: Long,
        reqDto: EndProjectReqDto,
    ) {
        val project =
            projectJpaRepository.findByIdOrNull(projectId)
                ?: throw ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (reqDto.endYear < project.startYear) {
            throw ExpectedException("종료 연도는 시작 연도보다 크거나 같아야 합니다.", HttpStatus.BAD_REQUEST)
        }
        project.status = ProjectStatus.ENDED
        project.endYear = reqDto.endYear
    }
}

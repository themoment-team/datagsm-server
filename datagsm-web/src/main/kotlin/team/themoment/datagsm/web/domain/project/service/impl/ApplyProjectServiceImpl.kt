package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectApplicationAssembler
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.ApplyProjectService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class ApplyProjectServiceImpl(
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
    private val projectApplicationAssembler: ProjectApplicationAssembler,
    private val projectEditRequestMapper: ProjectEditRequestMapper,
    private val currentUserProvider: CurrentUserProvider,
) : ApplyProjectService {
    @Transactional
    override fun execute(reqDto: ApplyProjectReqDto): ProjectEditRequestResDto {
        val applicant = currentUserProvider.getCurrentStudent()

        // 서로 다른 프로젝트를 동시에 신청할 수 있어야 하므로 신규 신청은 매번 새 행으로 만든다
        val request =
            ProjectEditRequestJpaEntity().apply {
                originalProject = null
                requestedBy = applicant
                requestStatus = ProjectRequestStatus.PENDING
            }
        projectApplicationAssembler.applyTo(request, reqDto)

        val savedRequest = projectEditRequestJpaRepository.save(request)
        return projectEditRequestMapper.toResDto(savedRequest)
    }
}

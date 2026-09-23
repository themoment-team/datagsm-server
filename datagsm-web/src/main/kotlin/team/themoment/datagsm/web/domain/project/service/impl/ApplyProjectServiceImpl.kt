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
import java.time.LocalDateTime

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

        // 승인 전 신규 신청은 신청자당 하나만 두고 재신청 시 기존 행을 덮어쓴다
        val request =
            projectEditRequestJpaRepository
                .findByOriginalProjectIsNullAndRequestedByIdAndRequestStatusNot(
                    applicant.id!!,
                    ProjectRequestStatus.ACCEPTED,
                ).orElseGet { ProjectEditRequestJpaEntity().apply { originalProject = null } }

        request.requestedBy = applicant
        request.requestedAt = LocalDateTime.now()
        request.requestStatus = ProjectRequestStatus.PENDING
        request.rejectReason = null
        request.processedAt = null
        projectApplicationAssembler.applyTo(request, reqDto)

        val savedRequest = projectEditRequestJpaRepository.save(request)
        return projectEditRequestMapper.toResDto(savedRequest)
    }
}

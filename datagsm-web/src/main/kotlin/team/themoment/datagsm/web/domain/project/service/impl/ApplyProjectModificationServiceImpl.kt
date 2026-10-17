package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.web.domain.project.mapper.ProjectApplicationAssembler
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.ApplyProjectModificationService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

@Service
class ApplyProjectModificationServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
    private val projectApplicationAssembler: ProjectApplicationAssembler,
    private val projectEditRequestMapper: ProjectEditRequestMapper,
    private val currentUserProvider: CurrentUserProvider,
) : ApplyProjectModificationService {
    @Transactional
    override fun execute(
        projectId: Long,
        reqDto: ApplyProjectReqDto,
    ): ProjectEditRequestResDto {
        val applicant = currentUserProvider.getCurrentStudent()
        val project =
            projectJpaRepository
                .findById(projectId)
                .orElseThrow { ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        if (!hasOwnership(project, applicant)) {
            throw ExpectedException("해당 프로젝트를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        // 프로젝트당 신청 행을 하나만 두기 위해 이전 상태와 무관하게 기존 행을 덮어쓴다
        val request =
            projectEditRequestJpaRepository
                .findByOriginalProjectId(projectId)
                .orElseGet { ProjectEditRequestJpaEntity().apply { originalProject = project } }

        request.requestedBy = applicant
        request.requestedAt = LocalDateTime.now()
        request.requestStatus = ProjectRequestStatus.PENDING
        request.rejectReason = null
        request.processedAt = null
        projectApplicationAssembler.applyTo(request, reqDto)

        val savedRequest = projectEditRequestJpaRepository.save(request)
        return projectEditRequestMapper.toResDto(savedRequest)
    }

    /** 참여자는 최초 신청자와 동일한 소유 권한을 가진다 */
    private fun hasOwnership(
        project: ProjectJpaEntity,
        student: StudentJpaEntity,
    ): Boolean = project.appliedBy?.id == student.id || project.participants.any { it.id == student.id }
}

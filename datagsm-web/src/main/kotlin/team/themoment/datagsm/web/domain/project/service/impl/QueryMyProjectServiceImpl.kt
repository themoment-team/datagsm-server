package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.request.QueryMyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.MyProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.MyProjectResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectMemberRole
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.QueryMyProjectService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.web.global.storage.ProjectIconStorage

@Service
class QueryMyProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
    private val projectEditRequestMapper: ProjectEditRequestMapper,
    private val projectIconStorage: ProjectIconStorage,
    private val currentUserProvider: CurrentUserProvider,
) : QueryMyProjectService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryMyProjectReqDto): MyProjectListResDto {
        val student = currentUserProvider.getCurrentStudent()
        val studentId = student.id!!

        val approvedProjects = projectJpaRepository.findAllByParticipantOrApplicant(studentId)
        val pendingByProjectId =
            projectEditRequestJpaRepository
                .findAllByOriginalProjectIdInAndRequestStatus(
                    approvedProjects.mapNotNull { it.id },
                    ProjectRequestStatus.PENDING,
                ).associateBy { it.originalProject!!.id }

        // 승인된 프로젝트는 항상 원본 1건으로만 노출하되, 대기 중인 수정안이 있으면 그 내용을 함께 내려준다
        val approvedResults =
            approvedProjects.map { project ->
                val pendingRequest = pendingByProjectId[project.id]
                if (pendingRequest == null) {
                    toResDto(project, studentId)
                } else {
                    toResDto(pendingRequest, studentId, project)
                }
            }

        // 아직 승인되지 않아 tb_project에 존재하지 않는 신규 생성 신청
        val standaloneResults =
            projectEditRequestJpaRepository
                .findAllByParticipantOrRequester(studentId)
                .filter { it.originalProject == null && it.requestStatus != ProjectRequestStatus.ACCEPTED }
                .map { toResDto(it, studentId, null) }

        val results =
            (approvedResults + standaloneResults)
                .filter { queryReq.requestStatus == null || it.requestStatus == queryReq.requestStatus }

        return MyProjectListResDto(totalElements = results.size, projects = results)
    }

    private fun toResDto(
        project: ProjectJpaEntity,
        studentId: Long,
    ): MyProjectResDto =
        MyProjectResDto(
            projectId = project.id,
            requestId = null,
            requestStatus = ProjectRequestStatus.ACCEPTED,
            rejectReason = null,
            role = resolveRole(project.appliedBy?.id, studentId),
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status,
            iconUrl = projectIconStorage.toIconUrl(project.iconKey),
            club = project.club?.let { projectEditRequestMapper.toClubSummary(it) },
            participants = project.participants.map { projectEditRequestMapper.toParticipantInfo(it) },
            repositories = project.repositories.toList(),
            techStacks = project.techStacks.toList(),
        )

    private fun toResDto(
        request: ProjectEditRequestJpaEntity,
        studentId: Long,
        originalProject: ProjectJpaEntity?,
    ): MyProjectResDto =
        MyProjectResDto(
            projectId = originalProject?.id,
            requestId = request.id,
            requestStatus = request.requestStatus,
            rejectReason = request.rejectReason,
            // 승인된 프로젝트는 원본 소유자 기준, 미승인 신청은 신청자 기준으로 역할을 판정한다
            role = resolveRole(originalProject?.appliedBy?.id ?: request.requestedBy.id, studentId),
            name = request.name,
            description = request.description,
            startYear = request.startYear,
            endYear = originalProject?.endYear,
            status = originalProject?.status,
            iconUrl = projectIconStorage.toIconUrl(request.iconKey),
            club = request.club?.let { projectEditRequestMapper.toClubSummary(it) },
            participants = request.participants.map { projectEditRequestMapper.toParticipantInfo(it) },
            repositories = request.repositories.toList(),
            techStacks = request.techStacks.toList(),
        )

    private fun resolveRole(
        ownerId: Long?,
        studentId: Long,
    ): ProjectMemberRole = if (ownerId == studentId) ProjectMemberRole.OWNER else ProjectMemberRole.PARTICIPANT
}

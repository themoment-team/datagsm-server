package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.internal.PublicParticipantInfoDto
import team.themoment.datagsm.common.domain.project.dto.request.QueryPublicProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.PublicProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.PublicProjectResDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.QueryPublicProjectService
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryPublicProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val projectEditRequestMapper: ProjectEditRequestMapper,
    private val projectIconStorage: ProjectIconStorage,
) : QueryPublicProjectService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryPublicProjectReqDto): PublicProjectListResDto {
        val projectPage =
            projectJpaRepository.searchProjectWithPaging(
                id = null,
                name = queryReq.projectName,
                clubId = queryReq.clubId,
                status = queryReq.status,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
                sortBy = queryReq.sortBy,
                sortDirection = queryReq.sortDirection,
            )

        return PublicProjectListResDto(
            totalPages = projectPage.totalPages,
            totalElements = projectPage.totalElements,
            projects = projectPage.content.map { toResDto(it) },
        )
    }

    @Transactional(readOnly = true)
    override fun executeById(projectId: Long): PublicProjectResDto {
        val project =
            projectJpaRepository
                .findById(projectId)
                .orElseThrow { ExpectedException("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        return toResDto(project)
    }

    private fun toResDto(project: ProjectJpaEntity): PublicProjectResDto =
        PublicProjectResDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status,
            iconUrl = projectIconStorage.toIconUrl(project.iconKey),
            deploymentUrl = project.deploymentUrl,
            club = project.club?.let { projectEditRequestMapper.toClubSummary(it) },
            participants =
                project.participants.map { PublicParticipantInfoDto(name = it.name, major = it.major) },
            repositories = project.repositories.toList(),
            techStacks = project.techStacks.toList(),
        )
}

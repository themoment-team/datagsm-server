package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectEditRequestReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.web.domain.project.mapper.ProjectEditRequestMapper
import team.themoment.datagsm.web.domain.project.service.QueryProjectEditRequestService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryProjectEditRequestServiceImpl(
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
    private val projectEditRequestMapper: ProjectEditRequestMapper,
) : QueryProjectEditRequestService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryProjectEditRequestReqDto): ProjectEditRequestListResDto {
        val requestPage =
            projectEditRequestJpaRepository.searchEditRequestWithPaging(
                requestStatus = queryReq.requestStatus,
                requestedById = null,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
            )

        return ProjectEditRequestListResDto(
            totalPages = requestPage.totalPages,
            totalElements = requestPage.totalElements,
            requests = requestPage.content.map { projectEditRequestMapper.toResDto(it) },
        )
    }

    @Transactional(readOnly = true)
    override fun executeById(requestId: Long): ProjectEditRequestResDto {
        val request =
            projectEditRequestJpaRepository
                .findById(requestId)
                .orElseThrow { ExpectedException("신청 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        return projectEditRequestMapper.toResDto(request)
    }
}

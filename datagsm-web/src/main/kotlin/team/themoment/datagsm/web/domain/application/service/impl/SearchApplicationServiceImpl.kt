package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.application.dto.request.SearchApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationListResDto
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.SearchApplicationService

@Service
class SearchApplicationServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
) : SearchApplicationService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: SearchApplicationReqDto): ApplicationListResDto {
        val applicationPage =
            applicationJpaRepository.searchApplicationWithPaging(
                name = queryReq.name,
                id = queryReq.id,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
            )

        return ApplicationListResDto(
            totalPages = applicationPage.totalPages,
            totalElements = applicationPage.totalElements,
            applications = applicationPage.content.map { it.toResDto() },
        )
    }
}

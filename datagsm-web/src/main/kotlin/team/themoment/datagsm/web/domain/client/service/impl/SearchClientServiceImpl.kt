package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.client.dto.request.SearchClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.ClientListResDto
import team.themoment.datagsm.common.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.SearchClientService

@Service
class SearchClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
) : SearchClientService {
    @Transactional(readOnly = true)
    override fun execute(reqDto: SearchClientReqDto): ClientListResDto {
        val clientPage =
            clientJpaRepository.searchClientWithPaging(
                name = reqDto.clientName,
                pageable = PageRequest.of(reqDto.page, reqDto.size),
            )

        return ClientListResDto(
            totalPages = clientPage.totalPages,
            totalElements = clientPage.totalElements,
            clients =
                clientPage.content.map { entity ->
                    ClientResDto(
                        id = entity.id,
                        name = entity.name,
                        redirectUrl = entity.redirectUrls,
                        scopes = entity.scopes,
                    )
                },
        )
    }
}

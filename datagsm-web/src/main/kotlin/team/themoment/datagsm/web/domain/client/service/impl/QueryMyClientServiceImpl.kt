package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.client.dto.response.ClientListResDto
import team.themoment.datagsm.common.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.QueryMyClientService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class QueryMyClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryMyClientService {
    @Transactional(readOnly = true)
    override fun execute(
        page: Int,
        size: Int,
    ): ClientListResDto {
        val currentAccount = currentUserProvider.getCurrentAccount()
        val pageable = PageRequest.of(page, size)
        val clientsPage = clientJpaRepository.findAllByAccountWithPaging(currentAccount, pageable)

        val clientResList =
            clientsPage.content.map { client ->
                ClientResDto(
                    id = client.id,
                    name = client.name,
                    redirectUrl = client.redirectUrls,
                    scopes = client.scopes,
                )
            }
        return ClientListResDto(
            totalPages = clientsPage.totalPages,
            totalElements = clientsPage.totalElements,
            clients = clientResList,
        )
    }
}

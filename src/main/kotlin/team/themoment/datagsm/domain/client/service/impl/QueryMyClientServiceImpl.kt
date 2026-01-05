package team.themoment.datagsm.domain.client.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.client.dto.response.ClientListResDto
import team.themoment.datagsm.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.QueryMyClientService
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
@Transactional(readOnly = true)
class QueryMyClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryMyClientService {
    override fun execute(): ClientListResDto {
        val currentAccount = currentUserProvider.getCurrentAccount()
        val clients = clientJpaRepository.findAllByAccount(currentAccount)

        val clientResList =
            clients.map { client ->
                ClientResDto(
                    id = client.id,
                    name = client.name,
                    redirectUrl = client.redirectUrls,
                )
            }
        return ClientListResDto(
            clients = clientResList,
            totalPages = 1,
            totalElements = clientResList.size.toLong(),
        )
    }
}

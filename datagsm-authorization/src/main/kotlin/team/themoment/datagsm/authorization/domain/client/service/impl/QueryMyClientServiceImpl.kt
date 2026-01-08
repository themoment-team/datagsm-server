package team.themoment.datagsm.authorization.domain.client.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.authorization.domain.client.dto.response.QueryMyClientResDto
import team.themoment.datagsm.authorization.domain.client.service.QueryMyClientService
import team.themoment.datagsm.authorization.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository

@Service
@Transactional(readOnly = true)
class QueryMyClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryMyClientService {
    override fun execute(): QueryMyClientResDto {
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
        return QueryMyClientResDto(
            clients = clientResList,
            totalElements = clientResList.size.toLong(),
        )
    }
}

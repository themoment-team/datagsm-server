package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.CreateClientResDto
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.CreateClientService
import team.themoment.datagsm.web.domain.client.service.QueryAvailableOauthScopesService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.UUID

@Service
class CreateClientServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val clientJpaRepository: ClientJpaRepository,
    private val queryAvailableOauthScopesService: QueryAvailableOauthScopesService,
) : CreateClientService {
    @Transactional
    override fun execute(reqDto: CreateClientReqDto): CreateClientResDto {
        val availableScopes =
            queryAvailableOauthScopesService
                .execute()
                .list
                .map { it.scope }
                .toSet()
        val invalidScopes = reqDto.scopes.filter { it !in availableScopes }

        if (invalidScopes.isNotEmpty()) {
            throw ExpectedException("허용되지 않는 OAuth 권한이 포함되어 있습니다.", HttpStatus.BAD_REQUEST)
        }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val rawSecret = generateUUID()

        val client =
            ClientJpaEntity().apply {
                id = generateUUID()
                secret = passwordEncoder.encode(rawSecret).toString()
                clientName = reqDto.clientName
                serviceName = reqDto.serviceName
                account = currentAccount
                redirectUrls = reqDto.redirectUrls
                scopes = reqDto.scopes
            }
        clientJpaRepository.save(client)

        return CreateClientResDto(
            clientId = client.id,
            clientSecret = rawSecret,
            clientName = client.clientName,
            serviceName = client.serviceName,
            redirectUrls = client.redirectUrls,
            scopes = client.scopes,
        )
    }

    private fun generateUUID(): String = UUID.randomUUID().toString()
}

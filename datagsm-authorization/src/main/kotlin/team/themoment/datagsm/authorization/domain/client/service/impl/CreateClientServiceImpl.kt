package team.themoment.datagsm.authorization.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.authorization.domain.client.dto.response.CreateClientResDto
import team.themoment.datagsm.authorization.domain.client.service.CreateClientService
import team.themoment.datagsm.authorization.domain.client.service.GetAvailableOauthScopesService
import team.themoment.datagsm.authorization.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.domain.client.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.sdk.exception.ExpectedException
import java.util.UUID

@Service
@Transactional
class CreateClientServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val clientJpaRepository: ClientJpaRepository,
    private val getAvailableOauthScopesService: GetAvailableOauthScopesService,
) : CreateClientService {
    override fun execute(reqDto: CreateClientReqDto): CreateClientResDto {
        val availableScopes = getAvailableOauthScopesService.execute()
        val invalidScopes = reqDto.scopes.minus(availableScopes)
        if (invalidScopes.isNotEmpty()) throw ExpectedException("허용되지 않는 OAuth 권한이 포함되어 있습니다: $invalidScopes", HttpStatus.BAD_REQUEST)
        val scopes =
            reqDto.scopes
                .map {
                    ApiScope.fromString(it)
                        ?: throw IllegalStateException("ApiScope는 허용된 $it 값을 포함하지 않습니다. See GetAvailableOauthScopesService")
                }.toSet()

        val currentAccount = currentUserProvider.getCurrentAccount()

        val rawSecret = generateUUID()

        val client =
            ClientJpaEntity().apply {
                id = generateUUID()
                secret = passwordEncoder.encode(rawSecret).toString()
                name = reqDto.name
                account = currentAccount
                redirectUrls = emptySet()
                this.scopes = scopes
            }
        clientJpaRepository.save(client)
        return CreateClientResDto(
            clientId = client.id,
            clientSecret = rawSecret,
            name = client.name,
            redirectUrls = client.redirectUrls,
        )
    }

    private fun generateUUID(): String = UUID.randomUUID().toString()
}

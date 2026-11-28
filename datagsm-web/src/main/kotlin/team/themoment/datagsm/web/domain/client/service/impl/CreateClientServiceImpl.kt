package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.CreateClientResDto
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.CreateClientService
import team.themoment.datagsm.web.domain.client.validator.OauthScopeValidator
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import java.util.UUID

@Service
class CreateClientServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthScopeValidator: OauthScopeValidator,
) : CreateClientService {
    @Transactional
    override fun execute(reqDto: CreateClientReqDto): CreateClientResDto {
        oauthScopeValidator.validate(reqDto.scopes)

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
                scopes.addAll(reqDto.scopes)
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

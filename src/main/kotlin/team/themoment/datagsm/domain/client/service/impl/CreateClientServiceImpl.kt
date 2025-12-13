package team.themoment.datagsm.domain.client.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.domain.client.dto.response.CreateClientResDto
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.CreateClientService
import team.themoment.datagsm.global.security.provider.CurrentUserProvider
import java.util.UUID

@Service
@Transactional
class CreateClientServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val clientJpaRepository: ClientJpaRepository,
) : CreateClientService {
    override fun execute(reqDto: CreateClientReqDto): CreateClientResDto {
        val currentAccount = currentUserProvider.getCurrentAccount()

        val rawSecret = generateUUID()

        val client =
            ClientJpaEntity().apply {
                id = generateUUID()
                secret = passwordEncoder.encode(rawSecret)
                name = reqDto.name
                account = currentAccount
                redirectUrl = emptyList()
            }
        clientJpaRepository.save(client)
        return CreateClientResDto(
            clientId = client.id,
            clientSecret = rawSecret,
            name = client.name,
            redirectUri = client.redirectUrl,
        )
    }

    private fun generateUUID(): String = UUID.randomUUID().toString()
}

package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.application.repository.ThirdPartyScopeJpaRepository
import team.themoment.datagsm.common.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.CreateClientResDto
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.CreateClientService
import team.themoment.datagsm.web.domain.client.util.ClientUtil
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.UUID

@Service
class CreateClientServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val clientJpaRepository: ClientJpaRepository,
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val thirdPartyScopeJpaRepository: ThirdPartyScopeJpaRepository,
) : CreateClientService {
    @Transactional
    override fun execute(reqDto: CreateClientReqDto): CreateClientResDto {
        val builtinScopes = ClientUtil.getAvailableOauthScopes()
        val thirdpartyScopeStrings = reqDto.scopes.filter { it.contains(':') && OAuthScope.fromString(it) == null }
        val invalidBuiltinScopes = reqDto.scopes.minus(builtinScopes).minus(thirdpartyScopeStrings.toSet())

        if (invalidBuiltinScopes.isNotEmpty()) {
            throw ExpectedException("허용되지 않는 OAuth 권한이 포함되어 있습니다: $invalidBuiltinScopes", HttpStatus.BAD_REQUEST)
        }

        thirdpartyScopeStrings.forEach { scopeStr ->
            val colonIdx = scopeStr.indexOf(':')
            if (colonIdx <= 0 || colonIdx == scopeStr.lastIndex) {
                throw ExpectedException("유효하지 않은 ThirdPartyScope 형식: $scopeStr", HttpStatus.BAD_REQUEST)
            }
            val appId = scopeStr.substring(0, colonIdx)
            val scopeName = scopeStr.substring(colonIdx + 1)
            applicationJpaRepository.findById(appId).orElseThrow {
                ExpectedException("존재하지 않는 Application: $appId", HttpStatus.BAD_REQUEST)
            }
            thirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(appId, scopeName)
                ?: throw ExpectedException("존재하지 않는 ThirdPartyScope: $scopeStr", HttpStatus.BAD_REQUEST)
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

package team.themoment.datagsm.authorization.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.authorization.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.authorization.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.authorization.domain.client.service.ModifyClientService
import team.themoment.datagsm.authorization.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class ModifyClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ModifyClientService {
    override fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto {
        val client =
            clientJpaRepository
                .findById(clientId)
                .orElseThrow { ExpectedException("Id에 해당하는 Client를 찾지 못했습니다.", HttpStatus.NOT_FOUND) }
        val currentAccount = currentUserProvider.getCurrentAccount()
        if (client.account != currentAccount) {
            throw ExpectedException("Client 변경 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }
        client.apply {
            name = reqDto.name ?: name
            redirectUrls = reqDto.redirectUrls ?: redirectUrls
        }

        return ClientResDto(
            id = client.id,
            name = client.name,
            redirectUrl = client.redirectUrls,
        )
    }
}

package team.themoment.datagsm.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.ModifyClientService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.checker.ScopeChecker
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
@Transactional
class ModifyClientServiceImpl(
    val clientJpaRepository: ClientJpaRepository,
    val currentUserProvider: CurrentUserProvider,
    val scopeChecker: ScopeChecker,
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
        if (
            client.account != currentAccount &&
            !scopeChecker.hasScope(currentUserProvider.getAuthentication(), ApiScope.CLIENT_MANAGE.scope)
        ) {
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

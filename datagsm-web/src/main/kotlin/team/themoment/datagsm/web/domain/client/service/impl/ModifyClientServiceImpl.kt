package team.themoment.datagsm.web.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.ClientResDto
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.client.service.ModifyClientService
import team.themoment.datagsm.web.domain.client.validator.OauthScopeValidator
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val oauthScopeValidator: OauthScopeValidator,
) : ModifyClientService {
    @Transactional
    override fun execute(
        clientId: String,
        reqDto: ModifyClientReqDto,
    ): ClientResDto {
        // scopes는 flush 시 스냅샷 대비 차이만 반영되므로, 동시 수정끼리 섞이지 않게 client 행을 잠그고 시작한다.
        val client =
            clientJpaRepository.findByIdForUpdate(clientId)
                ?: throw ExpectedException("Id에 해당하는 Client를 찾지 못했습니다.", HttpStatus.NOT_FOUND)
        val currentAccount = currentUserProvider.getCurrentAccount()

        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (client.account != currentAccount && !isAdmin) {
            throw ExpectedException("Client 변경 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        reqDto.scopes?.let { oauthScopeValidator.validate(it) }

        client.apply {
            clientName = reqDto.clientName ?: clientName
            serviceName = reqDto.serviceName ?: serviceName
            redirectUrls = reqDto.redirectUrls ?: redirectUrls
            reqDto.scopes?.let {
                scopes.clear()
                scopes.addAll(it)
            }
        }

        return ClientResDto(
            id = client.id,
            clientName = client.clientName,
            serviceName = client.serviceName,
            redirectUrl = client.redirectUrls,
            scopes = client.scopes,
        )
    }
}

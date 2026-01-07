package team.themoment.datagsm.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.domain.client.service.DeleteClientService
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.checker.ScopeChecker
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
@Transactional
class DeleteClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val scopeChecker: ScopeChecker,
) : DeleteClientService {
    override fun execute(clientId: String) {
        val client =
            clientJpaRepository
                .findById(clientId)
                .orElseThrow { ExpectedException("Id에 해당하는 Client를 찾지 못했습니다.", HttpStatus.NOT_FOUND) }
        val currentAccount = currentUserProvider.getCurrentAccount()
        if (
            client.account != currentAccount &&
            !scopeChecker.hasScope(currentUserProvider.getAuthentication(), ApiScope.CLIENT_MANAGE.scope)
        ) {
            throw ExpectedException("Client 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        clientJpaRepository.delete(client)
    }
}

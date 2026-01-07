package team.themoment.datagsm.authorization.domain.client.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.authorization.domain.client.service.DeleteClientService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.datagsm.authorization.global.security.provider.CurrentUserProvider

@Service
@Transactional
class DeleteClientServiceImpl(
    private val clientJpaRepository: ClientJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteClientService {
    override fun execute(clientId: String) {
        val client =
            clientJpaRepository
                .findById(clientId)
                .orElseThrow { ExpectedException("Id에 해당하는 Client를 찾지 못했습니다.", HttpStatus.NOT_FOUND) }
        val currentAccount = currentUserProvider.getCurrentAccount()
        if (client.account != currentAccount) {
            throw ExpectedException("Client 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        clientJpaRepository.delete(client)
    }
}

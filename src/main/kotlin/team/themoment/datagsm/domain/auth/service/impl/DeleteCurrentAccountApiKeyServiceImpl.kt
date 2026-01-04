package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.DeleteCurrentAccountApiKeyService
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
class DeleteCurrentAccountApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteCurrentAccountApiKeyService {
    @Transactional
    override fun execute() {
        val account = currentUserProvider.getCurrentAccount()
        apiKeyJpaRepository.deleteByAccount(account)
    }
}

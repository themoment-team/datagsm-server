package team.themoment.datagsm.domain.auth.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.DeleteApiKeyService
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

@Service
class DeleteApiKeyServiceImpl(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteApiKeyService {
    @Transactional
    override fun execute() {
        val student = currentUserProvider.getCurrentStudent()

        apiKeyJpaRepository.deleteByApiKeyStudent(student)
    }
}

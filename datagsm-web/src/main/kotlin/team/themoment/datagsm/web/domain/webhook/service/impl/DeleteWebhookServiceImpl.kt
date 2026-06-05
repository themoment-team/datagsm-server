package team.themoment.datagsm.web.domain.webhook.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.web.domain.webhook.service.DeleteWebhookService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteWebhookServiceImpl(
    private val webhookJpaRepository: WebhookJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteWebhookService {
    @Transactional
    override fun execute(webhookId: Long) {
        val account = currentUserProvider.getCurrentAccount()
        val webhook =
            webhookJpaRepository.findByIdAndAccount(webhookId, account)
                ?: throw ExpectedException("Webhook을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        webhookJpaRepository.delete(webhook)
    }
}

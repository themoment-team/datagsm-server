package team.themoment.datagsm.web.domain.webhook.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.webhook.dto.response.WebhookListResDto
import team.themoment.datagsm.common.domain.webhook.dto.response.WebhookResDto
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.web.domain.webhook.service.QueryWebhookService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class QueryWebhookServiceImpl(
    private val webhookJpaRepository: WebhookJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : QueryWebhookService {
    @Transactional(readOnly = true)
    override fun execute(): WebhookListResDto {
        val account = currentUserProvider.getCurrentAccount()
        val webhooks =
            webhookJpaRepository
                .findAllByAccount(account)
                .map { webhook ->
                    WebhookResDto(
                        id = webhook.id!!,
                        targetUrl = webhook.targetUrl,
                        events = webhook.events,
                        isActive = webhook.isActive,
                        createdAt = webhook.createdAt!!,
                    )
                }
        return WebhookListResDto(webhooks = webhooks)
    }
}

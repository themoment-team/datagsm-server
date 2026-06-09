package team.themoment.datagsm.web.domain.webhook.service

import team.themoment.datagsm.common.domain.webhook.dto.response.WebhookListResDto

interface QueryWebhookService {
    fun execute(): WebhookListResDto
}

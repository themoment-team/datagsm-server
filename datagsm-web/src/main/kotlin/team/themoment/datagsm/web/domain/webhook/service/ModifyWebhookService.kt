package team.themoment.datagsm.web.domain.webhook.service

import team.themoment.datagsm.common.domain.webhook.dto.request.ModifyWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.dto.response.WebhookResDto

interface ModifyWebhookService {
    fun execute(
        webhookId: Long,
        reqDto: ModifyWebhookReqDto,
    ): WebhookResDto
}

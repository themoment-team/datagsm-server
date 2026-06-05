package team.themoment.datagsm.web.domain.webhook.service

import team.themoment.datagsm.common.domain.webhook.dto.request.CreateWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.dto.response.CreateWebhookResDto

interface CreateWebhookService {
    fun execute(reqDto: CreateWebhookReqDto): CreateWebhookResDto
}

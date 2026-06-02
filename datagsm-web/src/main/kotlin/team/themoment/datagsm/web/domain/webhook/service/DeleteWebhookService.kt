package team.themoment.datagsm.web.domain.webhook.service

interface DeleteWebhookService {
    fun execute(webhookId: Long)
}

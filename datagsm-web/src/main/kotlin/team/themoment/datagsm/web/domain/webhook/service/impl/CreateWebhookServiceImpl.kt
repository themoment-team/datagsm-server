package team.themoment.datagsm.web.domain.webhook.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.webhook.dto.request.CreateWebhookReqDto
import team.themoment.datagsm.common.domain.webhook.dto.response.CreateWebhookResDto
import team.themoment.datagsm.common.domain.webhook.entity.WebhookJpaEntity
import team.themoment.datagsm.common.domain.webhook.repository.WebhookJpaRepository
import team.themoment.datagsm.common.domain.webhook.validator.WebhookUrlValidator
import team.themoment.datagsm.web.domain.webhook.service.CreateWebhookService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.security.SecureRandom

@Service
class CreateWebhookServiceImpl(
    private val webhookJpaRepository: WebhookJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateWebhookService {
    @Transactional
    override fun execute(reqDto: CreateWebhookReqDto): CreateWebhookResDto {
        val account = currentUserProvider.getCurrentAccount()

        if (webhookJpaRepository.countByAccount(account) >= MAX_WEBHOOKS_PER_ACCOUNT) {
            throw ExpectedException("Webhook은 최대 10개까지 등록할 수 있습니다.", HttpStatus.BAD_REQUEST)
        }

        if (WebhookUrlValidator.isPrivateOrLocalUrl(reqDto.targetUrl)) {
            throw ExpectedException("내부 네트워크 URL은 Webhook 수신 URL로 등록할 수 없습니다.", HttpStatus.BAD_REQUEST)
        }

        val secret = generateSecret()
        val webhook =
            WebhookJpaEntity().apply {
                targetUrl = reqDto.targetUrl
                events = reqDto.events.toMutableSet()
                this.account = account
                this.secret = secret
            }
        val saved = webhookJpaRepository.save(webhook)

        return CreateWebhookResDto(
            id = saved.id!!,
            targetUrl = saved.targetUrl,
            events = saved.events,
            isActive = saved.isActive,
            createdAt = saved.createdAt!!,
            secret = secret,
        )
    }

    private fun generateSecret(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val MAX_WEBHOOKS_PER_ACCOUNT = 10
    }
}

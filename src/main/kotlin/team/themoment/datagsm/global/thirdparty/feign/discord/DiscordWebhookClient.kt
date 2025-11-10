package team.themoment.datagsm.global.thirdparty.feign.discord

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import team.themoment.datagsm.global.common.discord.data.DiscordWebhookPayload

@Profile("stage", "prod")
@FeignClient(
    name = "discord-webhook",
    url = "\${spring.cloud.discord.webhook.url}",
)
interface DiscordWebhookClient {
    @PostMapping(
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    fun sendMessage(
        @RequestBody payload: DiscordWebhookPayload,
    )
}
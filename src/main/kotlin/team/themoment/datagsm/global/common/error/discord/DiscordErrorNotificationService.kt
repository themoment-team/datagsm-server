package team.themoment.datagsm.global.common.error.discord

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import team.themoment.datagsm.global.common.discord.data.DiscordEmbed
import team.themoment.datagsm.global.common.discord.data.DiscordField
import team.themoment.datagsm.global.common.discord.data.DiscordWebhookPayload
import team.themoment.datagsm.global.common.discord.data.EmbedColor
import team.themoment.datagsm.global.thirdparty.feign.discord.DiscordWebhookClient
import java.time.Instant

@Profile("stage", "prod")
@Component
class DiscordErrorNotificationService(
    private val discordWebhookClient: DiscordWebhookClient,
) {
    private val logger = LoggerFactory.getLogger(DiscordErrorNotificationService::class.java)

    companion object {
        private const val MAX_FIELD_LENGTH = 1000
    }

    fun notifyError(
        exception: Throwable,
        context: String? = null,
        additionalInfo: Map<String, Any> = emptyMap(),
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed = createErrorEmbed(exception, context, additionalInfo)
                val payload = DiscordWebhookPayload.embedMessage(embed)

                discordWebhookClient.sendMessage(payload)
            }.onFailure { sendException ->
                logger.error("Discord 에러 알림 전송 실패", sendException)
            }
        }
    }

    private fun createErrorEmbed(
        exception: Throwable,
        context: String?,
        additionalInfo: Map<String, Any>,
    ): DiscordEmbed {
        val fields =
            buildList {
                add(DiscordField("Exception Type", exception::class.simpleName ?: "Unknown", true))
                add(DiscordField("Message", exception.message?.truncateField() ?: "No message", false))

                context?.let {
                    add(DiscordField("Context", it.truncateField(), false))
                }

                exception.stackTrace.firstOrNull()?.let { stackElement ->
                    add(
                        DiscordField(
                            "Location",
                            "```${stackElement.fileName}:${stackElement.lineNumber} (${stackElement.methodName})```",
                            false,
                        ),
                    )
                }

                if (additionalInfo.isNotEmpty()) {
                    additionalInfo.forEach { (key, value) ->
                        add(DiscordField(key, value.toString().truncateField(), true))
                    }
                }

                val stackTrace =
                    exception.stackTrace
                        .take(5)
                        .joinToString("\n") { "at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }

                if (stackTrace.isNotEmpty()) {
                    add(DiscordField("Stack Trace", "```$stackTrace```", false))
                }
            }

        return DiscordEmbed(
            title = "🚨 애플리케이션 에러 발생",
            color = EmbedColor.ERROR.color,
            fields = fields,
            timestamp = Instant.now().toString(),
        )
    }

    private fun String.truncateField(): String =
        if (length > MAX_FIELD_LENGTH) {
            substring(0, MAX_FIELD_LENGTH) + "..."
        } else {
            this
        }
}
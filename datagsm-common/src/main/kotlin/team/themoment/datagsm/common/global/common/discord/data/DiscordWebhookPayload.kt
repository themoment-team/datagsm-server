package team.themoment.datagsm.common.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscordWebhookPayload(
    @field:JsonProperty("embeds") val embeds: List<DiscordEmbed>? = null,
    @field:JsonProperty("content") val content: String? = null,
) {
    companion object {
        fun embedMessage(embed: DiscordEmbed) =
            DiscordWebhookPayload(
                embeds = listOf(embed),
            )
    }
}

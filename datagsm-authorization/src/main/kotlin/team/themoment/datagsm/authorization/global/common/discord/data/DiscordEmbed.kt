package team.themoment.datagsm.authorization.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscordEmbed(
    @param:JsonProperty("title")
    val title: String? = null,
    @param:JsonProperty("color")
    val color: Int? = null,
    @param:JsonProperty("fields")
    val fields: List<DiscordField>? = null,
    @param:JsonProperty("timestamp")
    val timestamp: String? = null,
)

package team.themoment.datagsm.common.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscordEmbed(
    @field:JsonProperty("title")
    val title: String? = null,
    @field:JsonProperty("color")
    val color: Int? = null,
    @field:JsonProperty("fields")
    val fields: List<DiscordField>? = null,
    @field:JsonProperty("timestamp")
    val timestamp: String? = null,
)

package team.themoment.datagsm.common.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DiscordField(
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("value")
    val value: String,
    @field:JsonProperty("inline")
    val inline: Boolean = false,
)

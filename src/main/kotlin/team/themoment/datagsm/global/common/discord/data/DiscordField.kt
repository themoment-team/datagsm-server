package team.themoment.datagsm.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonProperty

data class DiscordField(
    @param:JsonProperty("name")
    val name: String,
    @param:JsonProperty("value")
    val value: String,
    @param:JsonProperty("inline")
    val inline: Boolean = false,
)
package team.themoment.datagsm.global.thirdparty.feign.google.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResDto(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,

    @JsonProperty("refresh_token")
    val refreshToken: String? = null,

    @JsonProperty("scope")
    val scope: String,

    @JsonProperty("id_token")
    val idToken: String? = null
)
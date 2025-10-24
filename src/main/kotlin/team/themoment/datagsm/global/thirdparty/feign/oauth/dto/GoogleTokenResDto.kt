package team.themoment.datagsm.global.thirdparty.feign.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResDto(
    @param:JsonProperty("access_token")
    val accessToken: String,
    @param:JsonProperty("token_type")
    val tokenType: String,
    @param:JsonProperty("expires_in")
    val expiresIn: Int,
    @param:JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @param:JsonProperty("scope")
    val scope: String,
    @param:JsonProperty("id_token")
    val idToken: String? = null,
)

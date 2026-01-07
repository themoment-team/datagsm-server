package team.themoment.datagsm.authorization.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class OauthTokenResDto(
    @param:Schema(description = "OAuth 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
    @param:Schema(description = "OAuth 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String,
)

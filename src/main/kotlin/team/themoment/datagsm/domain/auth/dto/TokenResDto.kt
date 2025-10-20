package team.themoment.datagsm.domain.auth.dto

data class TokenResDto(
    val accessToken: String,
    val refreshToken: String,
)

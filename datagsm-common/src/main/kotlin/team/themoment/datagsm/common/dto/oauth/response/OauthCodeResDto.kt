package team.themoment.datagsm.common.dto.oauth.response

import io.swagger.v3.oas.annotations.media.Schema

data class OauthCodeResDto(
    @param:Schema(description = "발급된 OAuth 인증 코드", example = "abc123xyz")
    val code: String,
)

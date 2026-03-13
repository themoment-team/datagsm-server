package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class JwkSetResDto(
    @field:Schema(description = "JWK 목록")
    val keys: List<RsaPublicJwkDto>,
)

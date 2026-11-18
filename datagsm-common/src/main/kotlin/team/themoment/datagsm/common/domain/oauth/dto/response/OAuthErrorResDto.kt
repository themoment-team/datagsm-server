package team.themoment.datagsm.common.domain.oauth.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OAuthErrorResDto(
    @field:JsonProperty("error")
    @field:Schema(
        description = "OAuth2 오류 코드",
        example = "invalid_grant",
        allowableValues = [
            "invalid_request",
            "invalid_client",
            "invalid_grant",
            "unauthorized_client",
            "unsupported_grant_type",
            "invalid_scope",
        ],
    )
    val error: String,
    @field:JsonProperty("error_description")
    @field:Schema(description = "오류 상세 설명. 없으면 필드를 생략합니다.", nullable = true)
    val errorDescription: String? = null,
    @field:JsonProperty("error_uri")
    @field:Schema(description = "오류 문서 URI. 현재 지정하지 않아 필드를 생략합니다.", nullable = true)
    val errorUri: String? = null,
)

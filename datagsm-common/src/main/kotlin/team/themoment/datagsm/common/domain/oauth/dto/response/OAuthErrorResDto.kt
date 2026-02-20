package team.themoment.datagsm.common.domain.oauth.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OAuthErrorResDto(
    @field:JsonProperty("error")
    @field:Schema(
        description = "OAuth2 error code",
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
    @field:Schema(description = "Human-readable error description")
    val errorDescription: String? = null,
    @field:JsonProperty("error_uri")
    @field:Schema(description = "URI for error documentation")
    val errorUri: String? = null,
)

package team.themoment.datagsm.common.domain.oauth.dto.request

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import tools.jackson.databind.annotation.JsonDeserialize

data class Oauth2TokenReqDto(
    @field:NotBlank(message = "grant_type은 필수입니다.")
    @param:Schema(
        description = "토큰 발급 방식",
        example = "authorization_code",
        allowableValues = ["authorization_code", "refresh_token", "client_credentials"],
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:JsonProperty("grant_type")
    @field:JsonAlias("grantType")
    val grantType: String = "",
    @param:Schema(
        description = "클라이언트 ID. 모든 grant에서 필요하며 client_secret_basic 사용 시 헤더로 전달할 수 있습니다.",
        example = "client-123",
        nullable = true,
    )
    @field:JsonProperty("client_id")
    @field:JsonAlias("clientId")
    val clientId: String? = null,
    @param:Schema(
        description = "클라이언트 secret. client_secret_basic과 중복 전달할 수 없습니다. PKCE 코드 교환·refresh_token 갱신에서는 생략 가능합니다.",
        nullable = true,
    )
    @field:JsonProperty("client_secret")
    @field:JsonAlias("clientSecret")
    val clientSecret: String? = null,
    @param:Schema(description = "authorization_code 방식에서 필수인 일회용 인가 코드", nullable = true)
    @field:JsonProperty("code")
    val code: String? = null,
    @param:Schema(
        description = "인가 코드 발급 시의 redirect_uri와 동일한 주소 (코드에 주소가 저장된 경우 필수)",
        example = "https://example.com/callback",
        nullable = true,
    )
    @field:JsonProperty("redirect_uri")
    @field:JsonAlias("redirectUri")
    val redirectUri: String? = null,
    @param:Schema(description = "refresh_token 방식에서 필수인 기존 Refresh Token", nullable = true)
    @field:JsonProperty("refresh_token")
    @field:JsonAlias("refreshToken")
    val refreshToken: String? = null,
    @param:Schema(
        description =
            "요청 권한 (공백 구분 문자열 또는 배열, appId:scopeName 형식). 코드 교환·갱신 시 기존 범위 이내로만 축소할 수 있습니다. " +
                "client_credentials는 허용 범위와의 교집합을 부여하며 생략 시 허용 범위 전체를 부여합니다.",
        example = "appId:account_read appId:student_read",
        nullable = true,
    )
    @field:JsonProperty("scope")
    @field:JsonDeserialize(using = OauthScopeSetDeserializer::class)
    val scope: Set<String>? = null,
    @param:Schema(description = "PKCE 인가 코드 교환 시 필수인 검증 값", example = "dBjftJeSSVPxgS31dKTHlEpQMZlzvvMpqHN0KT9LM5E", nullable = true)
    @field:JsonProperty("code_verifier")
    @field:JsonAlias("codeVerifier")
    val codeVerifier: String? = null,
)

package team.themoment.datagsm.oauth.authorization.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.Oauth2TokenResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthCodeResDto
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthAuthorizeFlowService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueOauthCodeService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.Oauth2TokenService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.StartOauthAuthorizeFlowService

@Tag(name = "OAuth", description = "OAuth 인증 관련 API")
@RestController
@RequestMapping("/v1/oauth")
class OauthController(
    val issueOauthCodeService: IssueOauthCodeService,
    val oauth2TokenService: Oauth2TokenService,
    val startOauthAuthorizeFlowService: StartOauthAuthorizeFlowService,
    val completeOauthAuthorizeFlowService: CompleteOauthAuthorizeFlowService,
) {
    @GetMapping("/authorize")
    @Operation(
        summary = "OAuth 인증 시작 (표준 플로우)",
        description = "OAuth 파라미터를 검증하고 프론트엔드 로그인 페이지로 리다이렉트합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "302", description = "로그인 페이지로 리다이렉트"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (invalid_request)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "존재하지 않는 클라이언트 (invalid_client)", content = [Content()]),
        ],
    )
    fun authorizeGet(
        @RequestParam("client_id") clientId: String?,
        @RequestParam("redirect_uri") redirectUri: String?,
        @RequestParam("response_type", defaultValue = "code") responseType: String?,
        @RequestParam("state", required = false) state: String?,
        @RequestParam("code_challenge", required = false) codeChallenge: String?,
        @RequestParam("code_challenge_method", required = false) codeChallengeMethod: String?,
    ): ResponseEntity<Void> =
        startOauthAuthorizeFlowService.execute(
            clientId,
            redirectUri,
            responseType,
            state,
            codeChallenge,
            codeChallengeMethod,
        )

    @PostMapping(
        "/authorize",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    @Operation(
        summary = "OAuth 인증 처리 (표준 플로우)",
        description = "사용자 인증 후 Authorization Code를 발급하고 외부 서비스로 리다이렉트합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "302", description = "외부 서비스로 리다이렉트"),
            ApiResponse(responseCode = "400", description = "세션 만료 또는 잘못된 요청", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
        ],
    )
    fun authorizePost(
        @Valid @RequestBody reqDto: OauthAuthorizeSubmitReqDto,
    ): ResponseEntity<Void> = completeOauthAuthorizeFlowService.execute(reqDto)

    @Deprecated("Use /v1/oauth/authorize for standard OAuth flow")
    @Operation(
        summary = "OAuth 인증 코드 발급 (레거시)",
        description = "⚠️ 레거시 엔드포인트입니다. 새로운 통합은 GET /v1/oauth/authorize를 사용하세요.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OAuth 인증 코드 발급 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패 / 등록되지 않은 Redirect URL)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "비밀번호 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "존재하지 않는 Client ID / 이메일", content = [Content()]),
        ],
    )
    @PostMapping("/code")
    fun issueOauthCode(
        @RequestBody @Valid reqDto: OauthCodeReqDto,
    ): OauthCodeResDto = issueOauthCodeService.execute(reqDto)

    @Operation(
        summary = "OAuth2 토큰 발급/갱신",
        description = "Authorization Code를 토큰으로 교환하거나 Refresh Token으로 갱신합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 파라미터 누락, 유효하지 않은 grant_type 등)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 client_secret, 유효하지 않은 토큰 등)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음 (code, client, account 등)", content = [Content()]),
        ],
    )
    @PostMapping(
        "/token",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun issueOauth2Token(
        @RequestBody @Valid reqDto: Oauth2TokenReqDto,
    ): Oauth2TokenResDto = oauth2TokenService.execute(reqDto)
}

package team.themoment.datagsm.oauth.authorization.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.Oauth2TokenResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthCodeResDto
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.IssueOauthCodeService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.Oauth2TokenService

@Tag(name = "OAuth", description = "OAuth 인증 관련 API")
@RestController
@RequestMapping("/v1/oauth")
class OauthController(
    val issueOauthCodeService: IssueOauthCodeService,
    val oauth2TokenService: Oauth2TokenService,
) {
    @Operation(summary = "OAuth 인증 코드 발급", description = "사용자 이메일, 비밀번호, 클라이언트 ID를 검증하여 OAuth 인증 코드를 발급합니다.")
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
        description = """
            RFC 6749 표준에 따른 OAuth2 토큰 엔드포인트.
            grant_type에 따라 다음 플로우를 지원합니다:
            - authorization_code: 인증 코드를 액세스/리프레시 토큰으로 교환
            - refresh_token: 리프레시 토큰으로 새로운 토큰 발급
            - client_credentials: 클라이언트 자격증명으로 토큰 발급 (Server-to-Server)
        """,
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

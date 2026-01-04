package team.themoment.datagsm.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.domain.oauth.dto.request.OauthTokenReqDto
import team.themoment.datagsm.domain.oauth.dto.request.RefreshOauthTokenReqDto
import team.themoment.datagsm.domain.oauth.dto.response.OauthCodeResDto
import team.themoment.datagsm.domain.oauth.dto.response.OauthTokenResDto
import team.themoment.datagsm.domain.oauth.service.ExchangeTokenService
import team.themoment.datagsm.domain.oauth.service.IssueOauthCodeService
import team.themoment.datagsm.domain.oauth.service.ReissueOauthTokenService

@Tag(name = "OAuth", description = "OAuth2 Authorization Server API")
@RestController
@RequestMapping("/v1/oauth")
class OauthController(
    val issueOauthCodeService: IssueOauthCodeService,
    val exchangeTokenService: ExchangeTokenService,
    val reissueOauthTokenService: ReissueOauthTokenService,
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

    @Operation(summary = "OAuth 토큰 교환", description = "인증 코드를 액세스 토큰과 리프레시 토큰으로 교환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OAuth 토큰 교환 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패 / Client Secret 불일치)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "존재하지 않거나 만료된 코드 / Client / 사용자", content = [Content()]),
        ],
    )
    @PostMapping("/token")
    fun exchangeToken(
        @RequestBody @Valid reqDto: OauthTokenReqDto,
    ): OauthTokenResDto = exchangeTokenService.execute(reqDto)

    @Operation(summary = "OAuth 토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OAuth 토큰 갱신 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "유효하지 않거나 일치하지 않는 토큰", content = [Content()]),
            ApiResponse(responseCode = "404", description = "저장된 Refresh Token을 찾을 수 없음 / 계정 없음", content = [Content()]),
        ],
    )
    @PutMapping("/refresh")
    fun refreshOauthToken(
        @RequestBody @Valid reqDto: RefreshOauthTokenReqDto,
    ): OauthTokenResDto = reissueOauthTokenService.execute(reqDto.refreshToken)
}

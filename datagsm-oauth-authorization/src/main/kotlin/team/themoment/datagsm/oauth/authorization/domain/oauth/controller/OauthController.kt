package team.themoment.datagsm.oauth.authorization.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthAuthorizeSubmitReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthConsentReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.IdpSessionListResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.JwkSetResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OAuthErrorResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.Oauth2TokenResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthConsentResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthSessionResDto
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentDataEditRequestReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentDataEditRequestResDto
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteIdpSessionHandoffService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthAuthorizeFlowService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.CompleteOauthConsentService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.DeleteIdpSessionService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.LogoutIdpSessionService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.Oauth2TokenService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryIdpSessionService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryJwkSetService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryOauthSessionService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryStudentDataEditRequestService
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.StartOauthAuthorizeFlowService
import team.themoment.datagsm.oauth.authorization.global.util.Oauth2TokenRequestReader
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "OAuth", description = "OAuth 인증 관련 API")
@RestController
@RequestMapping("/v1/oauth")
class OauthController(
    val oauth2TokenService: Oauth2TokenService,
    val startOauthAuthorizeFlowService: StartOauthAuthorizeFlowService,
    val completeOauthAuthorizeFlowService: CompleteOauthAuthorizeFlowService,
    val completeOauthConsentService: CompleteOauthConsentService,
    val completeIdpSessionHandoffService: CompleteIdpSessionHandoffService,
    val logoutIdpSessionService: LogoutIdpSessionService,
    val queryIdpSessionService: QueryIdpSessionService,
    val deleteIdpSessionService: DeleteIdpSessionService,
    val queryOauthSessionService: QueryOauthSessionService,
    val queryJwkSetService: QueryJwkSetService,
    val queryStudentDataEditRequestService: QueryStudentDataEditRequestService,
) {
    @GetMapping("/authorize")
    @Operation(
        summary = "OAuth 인증 시작",
        description =
            "OAuth 파라미터를 검증합니다. 유효한 SSO 세션이 없으면 프론트엔드 /oauth/authorize?token=, " +
                "요청 scope에 대한 동의가 없으면 /oauth/consent?token=으로 이동합니다. " +
                "SSO 세션과 동의가 모두 있으면 redirect_uri에 code와 요청 시 전달한 state를 붙여 이동합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "로그인·동의 화면 또는 클라이언트 콜백으로 이동",
                headers = [Header(name = "Location", description = "세션과 동의 여부에 따라 결정된 이동 주소", schema = Schema(type = "string"))],
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 인가 파라미터·scope는 OAuth 오류, 요청 검증 실패는 CommonApiResponse",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(oneOf = [OAuthErrorResDto::class, CommonApiResponse::class]),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "존재하지 않는 클라이언트 (invalid_client)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = OAuthErrorResDto::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "SSO 인가 코드 발급 요청 한도 초과",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
        ],
    )
    fun authorizeGet(
        @Valid @ModelAttribute queryReq: OauthAuthorizeReqDto,
        @CookieValue(name = "\${spring.security.oauth.idp-session-cookie-name}", required = false) sessionId: String?,
    ): ResponseEntity<Void> = startOauthAuthorizeFlowService.execute(queryReq, sessionId)

    @GetMapping("/authorize/session")
    @Operation(
        summary = "IdP 세션 확립",
        description =
            "로그인 직후 발급된 일회용 ticket과 verifier를 브라우저 최상위 이동으로 전달합니다. " +
                "티켓을 소비하고 Set-Cookie로 SSO 세션을 설정한 뒤 Location의 클라이언트 콜백으로 이동합니다. " +
                "Sec-Fetch-Mode는 navigate, Sec-Fetch-Site는 same-origin/same-site/cross-site/none을 허용하며, " +
                "두 헤더의 생략 허용 여부는 서버 설정에 따릅니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "세션 쿠키 설정 후 클라이언트 콜백으로 이동",
                headers = [
                    Header(name = "Location", description = "인가 코드와 선택적 state가 포함된 클라이언트 콜백 주소", schema = Schema(type = "string")),
                    Header(
                        name = "Set-Cookie",
                        description = "HttpOnly SSO 세션 쿠키 (Path=/, SameSite=Lax, Secure는 서버 설정에 따름)",
                        schema = Schema(type = "string"),
                    ),
                ],
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "400",
                description = "티켓 만료·재사용, verifier 불일치, 허용되지 않은 탐색 헤더 또는 콜백 주소",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = OAuthErrorResDto::class))],
            ),
        ],
    )
    fun completeIdpSessionHandoff(
        @Parameter(description = "로그인 처리 응답의 Location에 포함된 일회용 티켓", required = true)
        @RequestParam ticket: String,
        @Parameter(description = "티켓과 함께 발급된 검증 값. 바인딩은 선택이지만 누락 시 400입니다.")
        @RequestParam(required = false) verifier: String?,
        @RequestHeader(name = "Sec-Fetch-Site", required = false) secFetchSite: String?,
        @RequestHeader(name = "Sec-Fetch-Mode", required = false) secFetchMode: String?,
        @RequestHeader(name = "User-Agent", required = false) userAgent: String?,
    ): ResponseEntity<Void> = completeIdpSessionHandoffService.execute(ticket, verifier, secFetchSite, secFetchMode, userAgent)

    @PostMapping("/authorize")
    @Operation(
        summary = "OAuth 인증 처리",
        description =
            "사용자 인증 후 인가 코드를 발급하고 동의를 기록합니다. Location은 외부 서비스가 아니라 " +
                "{issuer}/v1/oauth/authorize/session?ticket=...&verifier=...이며, 브라우저가 이 주소로 최상위 이동해야 " +
                "SSO 쿠키 설정 후 클라이언트 콜백으로 이동합니다. 학생 정보 수정 요청이 있으면 요청된 필드만 반영합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "브라우저에서 열어야 할 IdP 세션 확립 주소 반환",
                headers = [Header(name = "Location", description = "ticket과 verifier가 포함된 IdP 세션 확립 주소", schema = Schema(type = "string"))],
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "400",
                description = "인가 세션 만료는 OAuth 오류, 요청 검증·본문 형식·학생 정보 오류는 CommonApiResponse",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(oneOf = [OAuthErrorResDto::class, CommonApiResponse::class]),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "이메일 또는 비밀번호 불일치",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "아직 승인되지 않은 계정",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "수정할 학생 또는 선택한 동아리가 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "변경할 학번이 이미 존재함",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "422",
                description = "요청된 학생 정보 수정 항목이 모두 입력되지 않음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "클라이언트 요청 한도 또는 계정 로그인 시도 횟수 초과",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "동의 정보를 저장하지 못함 또는 서버 내부 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
        ],
    )
    fun authorizePost(
        @Valid @RequestBody reqDto: OauthAuthorizeSubmitReqDto,
    ): ResponseEntity<Void> = completeOauthAuthorizeFlowService.execute(reqDto)

    @PostMapping("/authorize/consent")
    @Operation(
        summary = "OAuth 동의 처리",
        description =
            "SSO 세션이 있는 사용자가 요청된 scope를 승인하거나 거부합니다. " +
                "승인과 거부 모두 이동할 주소를 본문으로 반환하며, 거부 시 access_denied가 실려 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "승인·거부 모두 raw JSON의 redirectUrl 반환 (HTTP 리다이렉트 없음)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = OauthConsentResDto::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "인가 세션 만료는 OAuth 오류, 요청 검증·본문 형식 오류는 CommonApiResponse",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(oneOf = [OAuthErrorResDto::class, CommonApiResponse::class]),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "IdP 세션이 없거나 계정이 유효하지 않음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "승인 시 인가 코드 발급 요청 한도 초과",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "동의 정보를 저장하지 못함 또는 서버 내부 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
        ],
    )
    fun authorizeConsent(
        @Valid @RequestBody reqDto: OauthConsentReqDto,
        @CookieValue(name = "\${spring.security.oauth.idp-session-cookie-name}", required = false) sessionId: String?,
    ): OauthConsentResDto = completeOauthConsentService.execute(reqDto, sessionId)

    @PostMapping("/authorize/data-edit-requirements")
    @Operation(
        summary = "정보 수정 필요 항목 조회",
        description = "로그인이 정보 수정 요청으로 차단된 경우, 자격증명을 재검증한 뒤 입력받아야 할 필드 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않은 계정", content = [Content()]),
            ApiResponse(responseCode = "404", description = "진행 중인 수정 요청이 없음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "로그인 시도 횟수 초과", content = [Content()]),
        ],
    )
    fun queryStudentDataEditRequirements(
        @Valid @RequestBody reqDto: QueryStudentDataEditRequestReqDto,
    ): StudentDataEditRequestResDto = queryStudentDataEditRequestService.execute(reqDto)

    @PostMapping("/logout")
    @Operation(
        summary = "IdP 세션 로그아웃",
        description = "SSO 세션을 삭제하고 세션 쿠키를 만료시킵니다. 이미 발급된 access token은 만료 전까지 유효합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "로그아웃 완료 (세션이 없던 경우에도 동일)"),
        ],
    )
    fun logout(
        @CookieValue(name = "\${spring.security.oauth.idp-session-cookie-name}", required = false) sessionId: String?,
    ): ResponseEntity<Void> = logoutIdpSessionService.execute(sessionId)

    @GetMapping("/idp-sessions")
    @Operation(
        summary = "활성 IdP 세션 목록 조회",
        description = "현재 세션 쿠키가 가리키는 계정의 활성 세션 목록을 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "IdP 세션이 없거나 유효하지 않음", content = [Content()]),
        ],
    )
    fun queryIdpSessions(
        @CookieValue(name = "\${spring.security.oauth.idp-session-cookie-name}", required = false) sessionId: String?,
    ): IdpSessionListResDto = queryIdpSessionService.execute(sessionId)

    @DeleteMapping("/idp-sessions/{targetSessionHandle}")
    @Operation(
        summary = "개별 IdP 세션 종료",
        description =
            "세션 목록 조회가 반환한 sessionId(세션 ID의 SHA-256 해시)로 해당 세션을 종료합니다. " +
                "원본 세션 ID(쿠키 값)로는 404가 반환됩니다. 본인 계정의 세션만 종료할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "종료 완료"),
            ApiResponse(responseCode = "401", description = "IdP 세션이 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음", content = [Content()]),
        ],
    )
    fun deleteIdpSession(
        @PathVariable targetSessionHandle: String,
        @CookieValue(name = "\${spring.security.oauth.idp-session-cookie-name}", required = false) sessionId: String?,
    ): ResponseEntity<Void> = deleteIdpSessionService.execute(targetSessionHandle, sessionId)

    @GetMapping("/sessions/{token}")
    @Operation(
        summary = "OAuth 세션 조회",
        description = "세션 토큰을 기반으로 서비스 이름을 조회합니다. UI 스푸핑 방지를 위해 URL 파라미터의 service_name 대신 이 API를 사용합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "세션 조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않거나 만료된 세션 토큰 또는 존재하지 않는 클라이언트",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "세션에 저장된 scope에 대응하는 권한 정보가 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
        ],
    )
    fun queryOauthSession(
        @PathVariable token: String,
    ): OauthSessionResDto = queryOauthSessionService.execute(token)

    @Operation(
        summary = "JWK Set 조회",
        description = "JWT 서명 검증을 위한 RSA 공개키를 JWK Set 형식으로 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "RSA 공개키의 raw JWK Set 반환 (현재 use와 alg는 null)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = JwkSetResDto::class))],
            ),
        ],
    )
    @GetMapping("/jwks")
    fun getJwkSet(): JwkSetResDto = queryJwkSetService.execute()

    @Operation(
        summary = "OAuth2 토큰 발급/갱신",
        description =
            "authorization_code 교환, refresh_token 갱신, client_credentials 발급을 지원합니다. " +
                "application/json과 application/x-www-form-urlencoded를 받습니다. " +
                "클라이언트 인증은 본문의 client_secret 또는 Authorization: Basic 헤더(client_secret_basic) 중 하나만 사용합니다. " +
                "Basic 값은 client_id와 client_secret을 각각 form URL 인코딩한 뒤 콜론으로 이어 Base64 인코딩합니다. " +
                "PKCE 인가 코드 교환과 refresh_token 갱신은 client_secret 생략이 가능합니다. " +
                "scope는 appId:scopeName 형식이며 openid는 별도 프로토콜 값입니다. 갱신 시 기존 refresh token은 교체됩니다. " +
                "성공 본문은 래퍼 없는 JSON입니다. OAuth 오류·검증 실패와 CommonApiResponse 오류가 혼재합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Oauth2TokenReqDto::class)),
                    Content(mediaType = "application/x-www-form-urlencoded", schema = Schema(implementation = Oauth2TokenReqDto::class)),
                ],
            ),
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 발급 성공 (raw JSON)",
                headers = [
                    Header(name = "Cache-Control", schema = Schema(type = "string", example = "no-store")),
                    Header(name = "Pragma", schema = Schema(type = "string", example = "no-cache")),
                ],
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Oauth2TokenResDto::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "필수 값·grant_type·scope·PKCE 오류, 인가 코드 또는 refresh token 만료·재사용은 OAuth 오류. 잘못된 JSON은 CommonApiResponse",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(oneOf = [OAuthErrorResDto::class, CommonApiResponse::class]),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "클라이언트 없음·secret 불일치·잘못된 Basic 인증 정보 (invalid_client)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = OAuthErrorResDto::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "인가 코드 또는 refresh token에 연결된 사용자 계정이 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "클라이언트 요청 한도 초과",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "클라이언트의 권한 범위 데이터 오류 또는 서버 내부 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
        ],
    )
    @PostMapping("/token")
    fun issueOauth2Token(
        @RequestBody @Valid reqDto: Oauth2TokenReqDto,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        response: HttpServletResponse,
    ): Oauth2TokenResDto {
        preventTokenCaching(response)
        return oauth2TokenService.execute(Oauth2TokenRequestReader.applyBasicCredentials(reqDto, authorization))
    }

    // 표준 OAuth 라이브러리는 token 요청을 form 인코딩으로 보내므로 JSON 요청과 함께 받는다.
    @Operation(hidden = true)
    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun issueOauth2TokenWithForm(
        @RequestParam params: MultiValueMap<String, String>,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        response: HttpServletResponse,
    ): Oauth2TokenResDto {
        preventTokenCaching(response)
        val reqDto = Oauth2TokenRequestReader.fromForm(params)
        return oauth2TokenService.execute(Oauth2TokenRequestReader.applyBasicCredentials(reqDto, authorization))
    }

    // RFC 6749 5.1: 토큰이 담긴 응답은 중간 캐시나 브라우저에 저장되면 안 된다.
    private fun preventTokenCaching(response: HttpServletResponse) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store")
        response.setHeader(HttpHeaders.PRAGMA, "no-cache")
    }
}

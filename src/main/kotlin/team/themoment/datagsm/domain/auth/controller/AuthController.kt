package team.themoment.datagsm.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.request.OAuthCodeReqDto
import team.themoment.datagsm.domain.auth.dto.request.RefreshTokenReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeySearchResDto
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.domain.auth.dto.response.TokenResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.service.AuthenticateGoogleOAuthService
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.domain.auth.service.DeleteApiKeyService
import team.themoment.datagsm.domain.auth.service.ModifyApiKeyService
import team.themoment.datagsm.domain.auth.service.QueryApiKeyService
import team.themoment.datagsm.domain.auth.service.QueryApiScopeByScopeNameService
import team.themoment.datagsm.domain.auth.service.QueryApiScopeGroupService
import team.themoment.datagsm.domain.auth.service.ReissueTokenService
import team.themoment.datagsm.domain.auth.service.SearchApiKeyService
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.security.annotation.RequireScope

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authenticateGoogleOAuthService: AuthenticateGoogleOAuthService,
    private val createApiKeyService: CreateApiKeyService,
    private val deleteApiKeyService: DeleteApiKeyService,
    private val modifyApiKeyService: ModifyApiKeyService,
    private val queryApiKeyService: QueryApiKeyService,
    private val queryApiScopeByScopeNameService: QueryApiScopeByScopeNameService,
    private val queryApiScopeGroupService: QueryApiScopeGroupService,
    private val reissueTokenService: ReissueTokenService,
    private val searchApiKeyService: SearchApiKeyService,
) {
    @Operation(summary = "Google OAuth 인증", description = "Google OAuth 인증 코드로 토큰을 발급받습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "500", description = "Google OAuth 설정 오류", content = [Content()]),
        ],
    )
    @PostMapping("/google")
    fun authenticateWithGoogle(
        @RequestBody @Valid reqDto: OAuthCodeReqDto,
    ): TokenResDto = authenticateGoogleOAuthService.execute(reqDto.code)

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급받습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "재발급 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "저장된 토큰을 찾을 수 없음 / 토큰 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PutMapping("/refresh")
    fun refreshToken(
        @RequestBody @Valid reqDto: RefreshTokenReqDto,
    ): TokenResDto = reissueTokenService.execute(reqDto.refreshToken)

    @Operation(summary = "API 키 생성", description = "새로운 API 키를 생성합니다. scope를 지정하여 세부 권한을 설정할 수 있습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 생성 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음 / 유효하지 않은 scope", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 API 키가 존재함", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.AUTH_MANAGE)
    @PostMapping("/api-key")
    fun createApiKey(
        @RequestBody @Valid reqDto: CreateApiKeyReqDto,
    ): ApiKeyResDto = createApiKeyService.execute(reqDto)

    @Operation(summary = "API 키 갱신", description = "기존 API 키를 갱신합니다. 만료 15일 전부터 만료 15일 후까지만 갱신 가능하며, scope도 변경할 수 있습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 갱신 성공"),
            ApiResponse(responseCode = "400", description = "갱신 기간이 아님 / 학생 정보 없음 / 유효하지 않은 scope", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.AUTH_MANAGE)
    @PutMapping("/api-key")
    fun modifyApiKey(
        @RequestBody @Valid reqDto: ModifyApiKeyReqDto,
    ): ApiKeyResDto = modifyApiKeyService.execute(reqDto)

    @Operation(summary = "API 키 삭제", description = "기존 API 키를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 삭제 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.AUTH_MANAGE)
    @DeleteMapping("/api-key")
    fun deleteApiKey(): CommonApiResponse<Nothing> {
        deleteApiKeyService.execute()
        return CommonApiResponse.success("API 키가 삭제되었습니다.")
    }

    @Operation(summary = "API 키 조회", description = "현재 로그인한 사용자의 API 키를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 조회 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.AUTH_MANAGE)
    @GetMapping("/api-key")
    fun getApiKey(): ApiKeyResDto = queryApiKeyService.execute()

    @Operation(summary = "API 키 검색", description = "필터 조건에 맞는 API 키를 검색합니다. API 키는 마스킹되어 반환됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "검색 성공"),
        ],
    )
    @RequireScope(ApiScope.ADMIN_APIKEY)
    @GetMapping("/api-keys/search")
    fun searchApiKeys(
        @Parameter(description = "API 키 ID") @RequestParam(required = false) id: Long?,
        @Parameter(description = "계정 ID") @RequestParam(required = false) accountId: Long?,
        @Parameter(description = "권한 스코프") @RequestParam(required = false) scope: String?,
        @Parameter(description = "만료 여부") @RequestParam(required = false) isExpired: Boolean?,
        @Parameter(description = "갱신 가능 여부") @RequestParam(required = false) isRenewable: Boolean?,
        @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "100") size: Int,
    ): ApiKeySearchResDto =
        searchApiKeyService.execute(
            id,
            accountId,
            scope,
            isExpired,
            isRenewable,
            page,
            size,
        )

    @Operation(summary = "역할별 사용 가능한 API Scope 조회", description = "USER 또는 ADMIN 역할에서 사용 가능한 API Scope 목록을 카테고리별로 그룹핑하여 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/scopes")
    @RequireScope(ApiScope.AUTH_MANAGE)
    fun getApiScopes(
        @Parameter(description = "계정 역할 (USER 또는 ADMIN)", required = true)
        @RequestParam
        role: AccountRole,
    ): ApiScopeGroupListResDto = queryApiScopeGroupService.execute(role)

    @Operation(summary = "API Scope 단건 조회", description = "특정 API Scope의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 스코프", content = [Content()]),
        ],
    )
    @GetMapping("/scopes/{scopeName}")
    @RequireScope(ApiScope.AUTH_MANAGE)
    fun getApiScope(
        @Parameter(description = "조회할 스코프 이름", example = "student:read", required = true)
        @PathVariable
        scopeName: String,
    ): ApiScopeResDto = queryApiScopeByScopeNameService.execute(scopeName)
}

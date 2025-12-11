package team.themoment.datagsm.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.request.OAuthCodeReqDto
import team.themoment.datagsm.domain.auth.dto.request.RefreshTokenReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyRenewableResDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.domain.auth.dto.response.TokenResDto
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.service.AuthenticateGoogleOAuthService
import team.themoment.datagsm.domain.auth.service.CreateAdminApiKeyService
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.domain.auth.service.DeleteApiKeyService
import team.themoment.datagsm.domain.auth.service.ModifyAdminApiKeyService
import team.themoment.datagsm.domain.auth.service.ModifyApiKeyService
import team.themoment.datagsm.domain.auth.service.QueryApiKeyRenewableService
import team.themoment.datagsm.domain.auth.service.QueryApiKeyService
import team.themoment.datagsm.domain.auth.service.ReissueTokenService
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.security.annotation.RequireScope

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authenticateGoogleOAuthService: AuthenticateGoogleOAuthService,
    private val createApiKeyService: CreateApiKeyService,
    private val createAdminApiKeyService: CreateAdminApiKeyService,
    private val deleteApiKeyService: DeleteApiKeyService,
    private val modifyApiKeyService: ModifyApiKeyService,
    private val modifyAdminApiKeyService: ModifyAdminApiKeyService,
    private val queryApiKeyService: QueryApiKeyService,
    private val queryApiKeyRenewableService: QueryApiKeyRenewableService,
    private val reissueTokenService: ReissueTokenService,
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

    @Operation(summary = "API 키 갱신 가능 여부 조회", description = "현재 API 키가 갱신 가능한지 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.AUTH_MANAGE)
    @GetMapping("/api-key/renewable")
    fun checkApiKeyRenewable(): ApiKeyRenewableResDto = queryApiKeyRenewableService.execute()

    @Operation(summary = "Admin API 키 생성", description = "관리자용 API 키를 생성합니다. 모든 scope 사용 가능하며 만료 기간이 1년입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 생성 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음 / 유효하지 않은 scope", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 API 키가 존재함", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.ADMIN_APIKEY)
    @PostMapping("/api-key/admin")
    fun createAdminApiKey(
        @RequestBody @Valid reqDto: CreateApiKeyReqDto,
    ): ApiKeyResDto = createAdminApiKeyService.execute(reqDto)

    @Operation(summary = "Admin API 키 갱신", description = "관리자용 API 키를 갱신합니다. 모든 scope 사용 가능하며 만료 기간이 1년입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 갱신 성공"),
            ApiResponse(responseCode = "400", description = "갱신 기간이 아님 / 학생 정보 없음 / 유효하지 않은 scope", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.ADMIN_APIKEY)
    @PutMapping("/api-key/admin")
    fun modifyAdminApiKey(
        @RequestBody @Valid reqDto: ModifyApiKeyReqDto,
    ): ApiKeyResDto = modifyAdminApiKeyService.execute(reqDto)
}

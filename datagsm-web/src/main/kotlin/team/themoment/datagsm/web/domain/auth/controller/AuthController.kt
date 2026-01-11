package team.themoment.datagsm.web.domain.auth.controller

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
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.request.LoginReqDto
import team.themoment.datagsm.common.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.request.RefreshTokenReqDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeySearchResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.common.domain.auth.dto.response.TokenResDto
import team.themoment.datagsm.web.domain.auth.service.CreateCurrentAccountApiKeyService
import team.themoment.datagsm.web.domain.auth.service.DeleteApiKeyByIdService
import team.themoment.datagsm.web.domain.auth.service.DeleteCurrentAccountApiKeyService
import team.themoment.datagsm.web.domain.auth.service.LoginService
import team.themoment.datagsm.web.domain.auth.service.ModifyCurrentAccountApiKeyService
import team.themoment.datagsm.web.domain.auth.service.QueryApiScopeByScopeNameService
import team.themoment.datagsm.web.domain.auth.service.QueryApiScopeGroupService
import team.themoment.datagsm.web.domain.auth.service.QueryCurrentAccountApiKeyService
import team.themoment.datagsm.web.domain.auth.service.ReissueTokenService
import team.themoment.datagsm.web.domain.auth.service.SearchApiKeyService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val createCurrentAccountApiKeyService: CreateCurrentAccountApiKeyService,
    private val deleteCurrentAccountApiKeyService: DeleteCurrentAccountApiKeyService,
    private val deleteApiKeyByIdService: DeleteApiKeyByIdService,
    private val modifyCurrentAccountApiKeyService: ModifyCurrentAccountApiKeyService,
    private val queryCurrentAccountApiKeyService: QueryCurrentAccountApiKeyService,
    private val queryApiScopeByScopeNameService: QueryApiScopeByScopeNameService,
    private val queryApiScopeGroupService: QueryApiScopeGroupService,
    private val reissueTokenService: ReissueTokenService,
    private val searchApiKeyService: SearchApiKeyService,
    private val loginService: LoginService,
) {
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "비밀번호 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid reqDto: LoginReqDto,
    ): TokenResDto = loginService.execute(reqDto)

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
    @PostMapping("/api-key")
    fun createApiKey(
        @RequestBody @Valid reqDto: CreateApiKeyReqDto,
    ): ApiKeyResDto = createCurrentAccountApiKeyService.execute(reqDto)

    @Operation(summary = "API 키 갱신", description = "기존 API 키를 갱신합니다. 권한범위도 변경할 수 있으며 변경 시 새로 API 키가 발급됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 갱신 성공"),
            ApiResponse(responseCode = "400", description = "갱신 기간이 아님 / 학생 정보 없음 / 유효하지 않은 scope", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PutMapping("/api-key")
    fun modifyApiKey(
        @RequestBody @Valid reqDto: ModifyApiKeyReqDto,
    ): ApiKeyResDto = modifyCurrentAccountApiKeyService.execute(reqDto)

    @Operation(summary = "API 키 삭제", description = "현재 인증된 사용자의 API 키를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 삭제 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/api-key")
    fun deleteApiKey(): CommonApiResponse<Nothing> {
        deleteCurrentAccountApiKeyService.execute()
        return CommonApiResponse.success("API 키가 삭제되었습니다.")
    }

    @Operation(summary = "ID로 API 키 삭제", description = "특정 ID를 가진 API 키를 삭제합니다. 관리자 전용 API입니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 삭제 성공"),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/api-key/{id}")
    fun deleteApiKeyById(
        @Parameter(description = "삭제할 API 키 ID", required = true)
        @PathVariable
        id: Long,
    ): CommonApiResponse<Nothing> {
        deleteApiKeyByIdService.execute(id)
        return CommonApiResponse.success("API 키가 삭제되었습니다.")
    }

    @Operation(summary = "API 키 조회", description = "현재 로그인한 사용자의 API 키를 조회합니다. API 키는 마스킹되어 반환됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 조회 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @GetMapping("/api-key")
    fun getApiKey(): ApiKeyResDto = queryCurrentAccountApiKeyService.execute()

    @Operation(summary = "API 키 검색", description = "필터 조건에 맞는 API 키를 검색합니다. API 키는 마스킹되어 반환됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "검색 성공"),
        ],
    )
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

    @Operation(summary = "역할별 사용 가능한 API 권한 범위 조회", description = "USER 또는 ADMIN 역할에서 사용 가능한 API 권한 범위 목록을 카테고리별로 그룹핑하여 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/scopes")
    fun getApiScopes(
        @Parameter(description = "계정 역할 (USER 또는 ADMIN)", required = true)
        @RequestParam
        role: AccountRole,
    ): ApiScopeGroupListResDto = queryApiScopeGroupService.execute(role)

    @Operation(summary = "API 권한 범위 단건 조회", description = "특정 API 권한 범위의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 권한 범위", content = [Content()]),
        ],
    )
    @GetMapping("/scopes/{scopeName}")
    fun getApiScope(
        @Parameter(description = "조회할 권한 범위 이름", example = "student:read", required = true)
        @PathVariable
        scopeName: String,
    ): ApiScopeResDto = queryApiScopeByScopeNameService.execute(scopeName)
}

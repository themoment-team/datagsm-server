package team.themoment.datagsm.resource.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.ApiScope
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeySearchResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeGroupListResDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeResDto
import team.themoment.datagsm.resource.domain.auth.service.QueryApiScopeByScopeNameService
import team.themoment.datagsm.resource.domain.auth.service.QueryApiScopeGroupService
import team.themoment.datagsm.resource.domain.auth.service.QueryCurrentAccountApiKeyService
import team.themoment.datagsm.resource.domain.auth.service.SearchApiKeyService
import team.themoment.datagsm.resource.global.security.annotation.RequireScope

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val queryCurrentAccountApiKeyService: QueryCurrentAccountApiKeyService,
    private val queryApiScopeByScopeNameService: QueryApiScopeByScopeNameService,
    private val queryApiScopeGroupService: QueryApiScopeGroupService,
    private val searchApiKeyService: SearchApiKeyService,
) {
    @Operation(summary = "API 키 조회", description = "현재 로그인한 사용자의 API 키를 조회합니다. API 키는 마스킹되어 반환됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "API 키 조회 성공"),
            ApiResponse(responseCode = "400", description = "학생 정보 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "API 키를 찾을 수 없음 / 계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.AUTH_MANAGE)
    @GetMapping("/api-key")
    fun getApiKey(): ApiKeyResDto = queryCurrentAccountApiKeyService.execute()

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

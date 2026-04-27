package team.themoment.datagsm.web.domain.application.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.application.dto.request.AddOAuthScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.request.CreateApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.request.ModifyApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.request.ModifyOAuthScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.request.SearchApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationListResDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.web.domain.application.service.AddOAuthScopeService
import team.themoment.datagsm.web.domain.application.service.CreateApplicationService
import team.themoment.datagsm.web.domain.application.service.DeleteApplicationService
import team.themoment.datagsm.web.domain.application.service.DeleteOAuthScopeService
import team.themoment.datagsm.web.domain.application.service.ModifyApplicationService
import team.themoment.datagsm.web.domain.application.service.ModifyOAuthScopeService
import team.themoment.datagsm.web.domain.application.service.QueryApplicationService
import team.themoment.datagsm.web.domain.application.service.SearchApplicationService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Application", description = "Third-party Application 관련 API")
@RestController
@RequestMapping("/v1/applications")
class ApplicationController(
    private val createApplicationService: CreateApplicationService,
    private val modifyApplicationService: ModifyApplicationService,
    private val deleteApplicationService: DeleteApplicationService,
    private val queryApplicationService: QueryApplicationService,
    private val searchApplicationService: SearchApplicationService,
    private val addOAuthScopeService: AddOAuthScopeService,
    private val modifyOAuthScopeService: ModifyOAuthScopeService,
    private val deleteOAuthScopeService: DeleteOAuthScopeService,
) {
    @Operation(summary = "Application 목록 검색", description = "이름 또는 ID로 Application을 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
        ],
    )
    @GetMapping
    fun searchApplications(
        @Valid @ModelAttribute searchReq: SearchApplicationReqDto,
    ): ApplicationListResDto = searchApplicationService.execute(searchReq)

    @Operation(summary = "Application 단일 조회", description = "ID로 Application을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Application을 찾을 수 없음", content = [Content()]),
        ],
    )
    @GetMapping("/{id}")
    fun getApplication(
        @PathVariable id: String,
    ): ApplicationResDto = queryApplicationService.execute(id)

    @Operation(summary = "Application 생성", description = "새로운 Application을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
        ],
    )
    @PostMapping
    fun createApplication(
        @RequestBody @Valid reqDto: CreateApplicationReqDto,
    ): ApplicationResDto = createApplicationService.execute(reqDto)

    @Operation(summary = "Application 수정", description = "Application 이름을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Application을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{id}")
    fun modifyApplication(
        @PathVariable id: String,
        @RequestBody @Valid reqDto: ModifyApplicationReqDto,
    ): ApplicationResDto = modifyApplicationService.execute(id, reqDto)

    @Operation(summary = "Application 삭제", description = "Application을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Application을 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/{id}")
    fun deleteApplication(
        @PathVariable id: String,
    ): CommonApiResponse<Nothing> {
        deleteApplicationService.execute(id)
        return CommonApiResponse.success("Application을 성공적으로 삭제했습니다.")
    }

    @Operation(summary = "OAuth 권한 범위 추가", description = "Application에 권한 범위를 추가합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "추가 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Application을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping("/{id}/scopes")
    fun addScope(
        @PathVariable id: String,
        @RequestBody @Valid reqDto: AddOAuthScopeReqDto,
    ): ApplicationResDto = addOAuthScopeService.execute(id, reqDto)

    @Operation(summary = "OAuth 권한 범위 수정", description = "Application의 권한 범위를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Application 또는 OAuth 권한 범위를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{id}/scopes/{scopeId}")
    fun modifyScope(
        @PathVariable id: String,
        @PathVariable scopeId: Long,
        @RequestBody @Valid reqDto: ModifyOAuthScopeReqDto,
    ): ApplicationResDto = modifyOAuthScopeService.execute(id, scopeId, reqDto)

    @Operation(summary = "OAuth 권한 범위 삭제", description = "Application에서 권한 범위를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Application 또는 OAuth 권한 범위를 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/{id}/scopes/{scopeId}")
    fun deleteScope(
        @PathVariable id: String,
        @PathVariable scopeId: Long,
    ): CommonApiResponse<Nothing> {
        deleteOAuthScopeService.execute(id, scopeId)
        return CommonApiResponse.success("권한 범위를 성공적으로 삭제했습니다.")
    }
}

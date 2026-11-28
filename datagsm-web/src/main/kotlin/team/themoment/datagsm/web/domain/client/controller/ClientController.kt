package team.themoment.datagsm.web.domain.client.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.common.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.common.domain.client.dto.request.SearchClientReqDto
import team.themoment.datagsm.common.domain.client.dto.response.ClientListResDto
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeListResDto
import team.themoment.datagsm.web.domain.client.service.CreateClientService
import team.themoment.datagsm.web.domain.client.service.DeleteClientService
import team.themoment.datagsm.web.domain.client.service.ModifyClientService
import team.themoment.datagsm.web.domain.client.service.QueryAvailableOauthScopesService
import team.themoment.datagsm.web.domain.client.service.QueryMyClientService
import team.themoment.datagsm.web.domain.client.service.SearchClientService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Client", description = "OAuth 클라이언트 관련 API")
@RestController
@RequestMapping("/v1/clients")
class ClientController(
    private val createClientService: CreateClientService,
    private val modifyClientService: ModifyClientService,
    private val deleteClientService: DeleteClientService,
    private val queryMyClientService: QueryMyClientService,
    private val searchClientService: SearchClientService,
    private val queryAvailableOauthScopesService: QueryAvailableOauthScopesService,
) {
    @Operation(
        summary = "내 클라이언트 조회",
        description = "내가 등록한 클라이언트를 조회합니다. page는 0 이상, size는 1 이상이어야 하며 size 상한은 없습니다. 현재 범위 검증이 없어 page < 0 또는 size < 1이면 500이 발생합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "500", description = "page가 음수이거나 size가 1 미만인 경우", content = [Content()]),
        ],
    )
    @GetMapping("/my")
    fun getMyClient(
        @Parameter(description = "페이지 번호 (0 이상)", schema = Schema(minimum = "0"))
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기 (1 이상, 상한 없음)", schema = Schema(minimum = "1"))
        @RequestParam(required = false, defaultValue = "100") size: Int,
    ): ClientListResDto = queryMyClientService.execute(page, size)

    @Operation(summary = "어드민용 클라이언트 검색", description = "어드민이 등록된 클라이언트를 검색합니다. (ADMIN, ROOT 권한 필요)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
        ],
    )
    @GetMapping
    fun searchClient(
        @Valid @ModelAttribute queryReq: SearchClientReqDto,
    ): ClientListResDto = searchClientService.execute(queryReq)

    @Operation(summary = "클라이언트 생성", description = "새로운 OAuth 클라이언트를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
        ],
    )
    @PostMapping
    fun createClient(
        @RequestBody @Valid reqDto: CreateClientReqDto,
    ) = createClientService.execute(reqDto)

    @Operation(
        summary = "클라이언트 정보 수정",
        description = "기존 클라이언트의 정보를 수정합니다. scopes를 포함하면 클라이언트의 권한 목록을 해당 값으로 교체합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (검증 실패 또는 허용되지 않는 OAuth 권한 포함)",
                content = [Content()],
            ),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "클라이언트를 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{clientId}")
    fun modifyClient(
        @PathVariable clientId: String,
        @RequestBody @Valid reqDto: ModifyClientReqDto,
    ) = modifyClientService.execute(clientId, reqDto)

    @Operation(summary = "클라이언트 삭제", description = "기존 클라이언트를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "클라이언트를 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/{clientId}")
    fun deleteClient(
        @PathVariable clientId: String,
    ): CommonApiResponse<Nothing> {
        deleteClientService.execute(clientId)
        return CommonApiResponse.success("Client를 성공적으로 삭제했습니다.")
    }

    @Operation(
        summary = "사용 가능한 OAuth 권한 범위 조회",
        description =
            "applicationName을 포함한 사용 가능 권한 범위의 평면 목록입니다. " +
                "scope는 {applicationId}:{scopeName} 형식이며 클라이언트 생성 요청에 그대로 사용합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = [Content()]),
        ],
    )
    @GetMapping("/available-scopes")
    fun getAvailableScopes(): OAuthScopeListResDto = queryAvailableOauthScopesService.execute()
}

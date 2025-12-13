package team.themoment.datagsm.domain.client.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.client.dto.request.CreateClientReqDto
import team.themoment.datagsm.domain.client.dto.request.ModifyClientReqDto
import team.themoment.datagsm.domain.client.dto.response.ClientListResDto
import team.themoment.datagsm.domain.client.service.CreateClientService
import team.themoment.datagsm.domain.client.service.DeleteClientService
import team.themoment.datagsm.domain.client.service.ModifyClientService
import team.themoment.datagsm.domain.client.service.QueryMyClientService
import team.themoment.datagsm.domain.client.service.SearchClientService
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.security.annotation.RequireScope

@Tag(name = "Client", description = "OAuth 클라이언트 관련 API")
@RestController
@RequestMapping("/v1/client")
class ClientController(
    private val createClientService: CreateClientService,
    private val modifyClientService: ModifyClientService,
    private val deleteClientService: DeleteClientService,
    private val queryMyClientService: QueryMyClientService,
    private val searchClientService: SearchClientService,
) {
    @Operation(summary = "내 클라이언트 조회", description = "내가 등록한 클라이언트를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),

        ],
    )
    @GetMapping
    fun getMyClient() = queryMyClientService.execute()

    @Operation(summary = "어드민용 클라이언트 검색", description = "어드민이 등록된 클라이언트를 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청"),
        ],
    )
    @RequireScope(ApiScope.CLIENT_MANAGE)
    @GetMapping("/search")
    fun searchClient(
        @Parameter(description = "클라이언트 이름") @RequestParam(required = false) clientName: String?,
        @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "100") size: Int,
    ): ClientListResDto = searchClientService.execute(clientName, page, size)

    @Operation(summary = "클라이언트 생성", description = "새로운 OAuth 클라이언트를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        ],
    )
    @PostMapping
    fun createClient(
        @RequestBody @Valid reqDto: CreateClientReqDto,
    ) = createClientService.execute(reqDto)

    @Operation(summary = "클라이언트 정보 수정", description = "기존 클라이언트의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청"),
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
            ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            ApiResponse(responseCode = "403", description = "권한이 없는 요청"),
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
}

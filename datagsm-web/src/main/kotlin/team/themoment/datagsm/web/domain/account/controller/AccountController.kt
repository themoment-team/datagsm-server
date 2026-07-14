package team.themoment.datagsm.web.domain.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.dto.request.DeleteMyAccountReqDto
import team.themoment.datagsm.common.domain.account.dto.request.ModifyAccountRoleReqDto
import team.themoment.datagsm.common.domain.account.dto.request.QueryAccountReqDto
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.common.domain.account.dto.response.AccountListResDto
import team.themoment.datagsm.common.domain.account.dto.response.AccountResDto
import team.themoment.datagsm.web.domain.account.service.ApproveTeacherAccountService
import team.themoment.datagsm.web.domain.account.service.DeleteMyAccountService
import team.themoment.datagsm.web.domain.account.service.ModifyAccountRoleService
import team.themoment.datagsm.web.domain.account.service.QueryAccountDetailService
import team.themoment.datagsm.web.domain.account.service.QueryAccountService
import team.themoment.datagsm.web.domain.account.service.QueryMyInfoService

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequestMapping("/v1/accounts")
class AccountController(
    private val queryMyInfoService: QueryMyInfoService,
    private val deleteMyAccountService: DeleteMyAccountService,
    private val queryAccountService: QueryAccountService,
    private val queryAccountDetailService: QueryAccountDetailService,
    private val modifyAccountRoleService: ModifyAccountRoleService,
    private val approveTeacherAccountService: ApproveTeacherAccountService,
) {
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 계정 및 학생 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "API Key 인증으로는 접근 불가", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @GetMapping("/my")
    fun getMyInfo(): AccountInfoResDto = queryMyInfoService.execute()

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 계정을 탈퇴합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            ApiResponse(responseCode = "401", description = "비밀번호 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @DeleteMapping("/my")
    fun deleteMyAccount(
        @Valid @RequestBody reqDto: DeleteMyAccountReqDto,
    ) = deleteMyAccountService.execute(reqDto)

    @Operation(summary = "계정 목록 조회", description = "필터 조건에 맞는 계정 목록을 조회합니다. 각 계정에 연결된 학생 정보가 포함됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @GetMapping
    fun getAccounts(
        @Valid @ModelAttribute queryReq: QueryAccountReqDto,
    ): AccountListResDto = queryAccountService.execute(queryReq)

    @Operation(summary = "계정 단건 조회", description = "특정 계정의 정보를 조회합니다. 연결된 학생 정보가 포함됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @GetMapping("/{accountId}")
    fun getAccount(
        @Parameter(description = "계정 ID") @PathVariable accountId: Long,
    ): AccountResDto = queryAccountDetailService.execute(accountId)

    @Operation(summary = "계정 권한 변경", description = "특정 계정의 역할을 변경합니다. 본인 및 최고 관리자 계정은 변경할 수 없습니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "권한 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "403", description = "본인 또는 최고 관리자 계정 변경 불가", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{accountId}/role")
    fun modifyAccountRole(
        @Parameter(description = "계정 ID") @PathVariable accountId: Long,
        @RequestBody @Valid reqDto: ModifyAccountRoleReqDto,
    ) = modifyAccountRoleService.execute(accountId, reqDto)

    @Operation(summary = "선생님 계정 승인", description = "승인 대기 중인 선생님 계정을 활성화합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "승인 성공"),
            ApiResponse(responseCode = "400", description = "선생님 계정이 아님", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 승인된 계정", content = [Content()]),
        ],
    )
    @PatchMapping("/{accountId}/approval")
    fun approveTeacherAccount(
        @Parameter(description = "계정 ID") @PathVariable accountId: Long,
    ) = approveTeacherAccountService.execute(accountId)
}

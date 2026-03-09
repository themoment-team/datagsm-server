package team.themoment.datagsm.web.domain.utility.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.web.domain.utility.service.ModifyAccountRoleService
import team.themoment.sdk.response.CommonApiResponse

@Tag(name = "Utility", description = "개발용 유틸리티 API")
@RestController
@RequestMapping("/v1/utility")
@Profile("!prod")
class UtilityController(
    private val modifyAccountRoleService: ModifyAccountRoleService,
) {
    @Operation(summary = "계정 권한 변경", description = "입력된 이메일에 해당하는 계정의 권한을 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "계정 권한 변경 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 역할 값", content = [Content()]),
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/accounts/role")
    fun modifyAccountRole(
        @Parameter(description = "권한을 변경할 계정의 이메일", required = true)
        @RequestParam email: String,
        @Parameter(description = "변경할 계정 권한", required = true)
        @RequestParam role: AccountRole,
    ): CommonApiResponse<Nothing> = modifyAccountRoleService.execute(email, role)
}

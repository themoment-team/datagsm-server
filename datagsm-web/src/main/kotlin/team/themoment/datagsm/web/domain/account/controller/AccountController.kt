package team.themoment.datagsm.web.domain.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.dto.response.GetMyInfoResDto
import team.themoment.datagsm.web.domain.account.service.GetMyInfoService

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequestMapping("/v1/accounts")
class AccountController(
    private val getMyInfoService: GetMyInfoService,
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
    fun getMyInfo(): GetMyInfoResDto = getMyInfoService.execute()
}

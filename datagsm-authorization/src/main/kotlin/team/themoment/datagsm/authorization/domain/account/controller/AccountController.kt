package team.themoment.datagsm.authorization.domain.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.authorization.domain.account.service.GetMyInfoService
import team.themoment.datagsm.common.domain.account.dto.response.GetMyInfoResDto

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequestMapping("/v1/accounts")
class AccountController(
    private val getMyInfoService: GetMyInfoService,
) {
    @GetMapping("/my")
    @Deprecated(
        message = "Use GET /userinfo on datagsm-userinfo (port 8083) instead",
        replaceWith = ReplaceWith("GET http://localhost:8083/userinfo"),
    )
    @Operation(
        summary = "[Deprecated] 내 정보 조회",
        description = "새 클라이언트는 datagsm-userinfo의 /userinfo 엔드포인트를 사용하세요.",
    )
    fun getMyInfo(): GetMyInfoResDto = getMyInfoService.execute()
}

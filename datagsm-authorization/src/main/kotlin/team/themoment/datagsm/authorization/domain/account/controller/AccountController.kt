package team.themoment.datagsm.authorization.domain.account.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.authorization.domain.account.service.GetMyInfoService
import team.themoment.datagsm.common.domain.account.dto.response.GetMyInfoResDto

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequestMapping("/v1/account")
class AccountController(
    private val getMyInfoService: GetMyInfoService,
) {
    @GetMapping("/my")
    fun getMyInfo(): GetMyInfoResDto = getMyInfoService.execute()
}

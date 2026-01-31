package team.themoment.datagsm.oauth.userinfo.domain.userinfo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.dto.response.GetMyInfoResDto
import team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.GetUserInfoService

@Tag(name = "UserInfo", description = "OAuth2 UserInfo 엔드포인트")
@RestController
class UserInfoController(
    private val getUserInfoService: GetUserInfoService,
) {
    @GetMapping("/userinfo")
    @PreAuthorize("hasAuthority('SCOPE_self:read')")
    @Operation(
        summary = "사용자 정보 조회",
        description = "OAuth2 Access Token을 사용하여 현재 사용자 정보를 조회합니다.",
    )
    fun getUserInfo(): GetMyInfoResDto = getUserInfoService.execute()
}

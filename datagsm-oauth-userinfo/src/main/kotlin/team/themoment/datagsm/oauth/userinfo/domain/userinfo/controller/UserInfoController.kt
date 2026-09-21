package team.themoment.datagsm.oauth.userinfo.domain.userinfo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.QueryUserInfoService

@Tag(name = "UserInfo", description = "OAuth 사용자 정보 관련 API")
@RestController
class UserInfoController(
    private val queryUserInfoService: QueryUserInfoService,
) {
    // OIDC UserInfo 표준 준수를 위한 예외 엔드포인트: 다른 공개 API와 달리 /v1 버전 prefix가 없고,
    // CommonApiResponse로 감싸지 않은 raw 클레임 JSON을 반환한다 (application.yml의 not-wrapping-urls 참고).
    @GetMapping("/userinfo")
    @Operation(
        summary = "사용자 정보 조회",
        description =
            "OAuth2 Access Token을 사용하여 현재 사용자 정보를 조회합니다. " +
                "student/teacher는 student_read(또는 하위 호환 self_read), clubs는 club_read, projects는 project_read " +
                "스코프를 보유한 경우에만 값이 채워지며, 미보유 시 각각 null 또는 빈 목록으로 반환됩니다.",
    )
    fun getUserInfo(): AccountInfoResDto = queryUserInfoService.execute()
}

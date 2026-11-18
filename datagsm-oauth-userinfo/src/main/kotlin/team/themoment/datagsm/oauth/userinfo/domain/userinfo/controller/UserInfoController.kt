package team.themoment.datagsm.oauth.userinfo.domain.userinfo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.QueryUserInfoService
import team.themoment.sdk.response.CommonApiResponse

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
            "OAuth2 Access Token을 사용하여 현재 사용자 정보를 raw JSON으로 조회합니다. " +
                "진입 시 appId:account_read 또는 하위 호환 appId:self_read가 필요하며 student_read만으로는 조회할 수 없습니다. " +
                "student/teacher는 student_read(또는 하위 호환 self_read), clubs는 club_read, projects는 project_read " +
                "스코프를 보유한 경우에만 값이 채워지며, 미보유 시 각각 null 또는 빈 목록으로 반환됩니다. " +
                "student 안의 majorClub/autonomousClub은 club_read 또는 self_read를 보유한 경우에만 채워집니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "권한 범위에 따른 사용자 정보 조회 성공 (raw JSON)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountInfoResDto::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Access Token이 없거나 유효하지 않음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "account_read 또는 self_read 권한이 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "토큰에 연결된 사용자 계정이 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "클라이언트 요청 한도 초과",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CommonApiResponse::class))],
            ),
        ],
    )
    fun getUserInfo(): AccountInfoResDto = queryUserInfoService.execute()
}

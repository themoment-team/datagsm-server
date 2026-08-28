package team.themoment.datagsm.oauth.authorization.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.club.dto.response.ClubSummaryListResDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.oauth.authorization.domain.club.service.QueryPublicClubListService

@Tag(name = "Club", description = "동아리 관련 API")
@RestController
@RequestMapping("/v1/oauth/clubs")
class ClubController(
    private val queryPublicClubListService: QueryPublicClubListService,
) {
    @Operation(
        summary = "동아리 목록 조회 (로그인 전)",
        description = "로그인 화면 및 정보 수정 화면에서 전공/자율 동아리 선택지를 채우기 위한 비인증 조회 API입니다. 운영 중인 동아리만 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)"),
        ],
    )
    @GetMapping
    fun getClubs(
        @RequestParam type: ClubType,
    ): ClubSummaryListResDto = queryPublicClubListService.execute(type)
}

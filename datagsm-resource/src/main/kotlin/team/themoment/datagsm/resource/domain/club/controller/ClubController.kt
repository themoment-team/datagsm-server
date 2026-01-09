package team.themoment.datagsm.resource.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.domain.club.ClubSortBy
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.dto.club.response.ClubListResDto
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.club.service.QueryClubService
import team.themoment.datagsm.resource.global.security.annotation.RequireScope

@Tag(name = "Club", description = "동아리 관련 API")
@RestController
@RequestMapping("/v1/clubs")
class ClubController(
    private val queryClubService: QueryClubService,
) {
    @Operation(summary = "동아리 정보 조회", description = "필터 조건에 맞는 동아리 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @RequireScope(ApiScope.CLUB_READ)
    @GetMapping
    fun getClubInfo(
        @Parameter(description = "동아리 ID") @RequestParam(required = false) clubId: Long?,
        @Parameter(description = "동아리 이름") @RequestParam(required = false) clubName: String?,
        @Parameter(description = "동아리 종류") @RequestParam(required = false) clubType: ClubType?,
        @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "100") size: Int,
        @Parameter(description = "부장을 부원 목록에 포함할지 여부") @RequestParam(required = false, defaultValue = "false") includeLeaderInParticipants:
            Boolean,
        @Parameter(description = "정렬 기준 (ID, NAME, TYPE)") @RequestParam(required = false) sortBy: ClubSortBy?,
        @Parameter(description = "정렬 방향 (ASC, DESC)") @RequestParam(required = false, defaultValue = "ASC") sortDirection: SortDirection,
    ): ClubListResDto = queryClubService.execute(clubId, clubName, clubType, page, size, includeLeaderInParticipants, sortBy, sortDirection)
}

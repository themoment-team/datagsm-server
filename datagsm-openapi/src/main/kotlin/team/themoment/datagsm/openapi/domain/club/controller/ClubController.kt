package team.themoment.datagsm.openapi.domain.club.controller

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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.request.QueryClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.openapi.domain.club.service.CreateClubService
import team.themoment.datagsm.openapi.domain.club.service.DeleteClubService
import team.themoment.datagsm.openapi.domain.club.service.ModifyClubService
import team.themoment.datagsm.openapi.domain.club.service.QueryClubService
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope

@Tag(name = "Club", description = "동아리 관련 API")
@RestController
@RequestMapping("/v1/clubs")
class ClubController(
    private val queryClubService: QueryClubService,
    private val createClubService: CreateClubService,
    private val modifyClubService: ModifyClubService,
    private val deleteClubService: DeleteClubService,
) {
    @Operation(summary = "동아리 정보 조회", description = "필터 조건에 맞는 동아리 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.CLUB_READ)
    @GetMapping
    fun getClubInfo(
        @Valid @ModelAttribute queryReq: QueryClubReqDto,
    ): ClubListResDto = queryClubService.execute(queryReq)

    @Operation(summary = "동아리 생성", description = "새로운 동아리를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 동아리", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.CLUB_WRITE)
    @PostMapping
    fun createClub(
        @RequestBody @Valid clubReqDto: ClubReqDto,
    ): ClubResDto = createClubService.execute(clubReqDto)

    @Operation(summary = "동아리 정보 수정", description = "기존 동아리의 정보를 전체 교체합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.CLUB_WRITE)
    @PutMapping("/{clubId}")
    fun updateClub(
        @Parameter(description = "동아리 ID") @PathVariable clubId: Long,
        @RequestBody @Valid clubReqDto: ClubReqDto,
    ): ClubResDto = modifyClubService.execute(clubId, clubReqDto)

    @Operation(summary = "동아리 삭제", description = "기존 동아리를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "삭제 성공"),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.CLUB_WRITE)
    @DeleteMapping("/{clubId}")
    fun deleteClub(
        @Parameter(description = "동아리 ID") @PathVariable clubId: Long,
    ) = deleteClubService.execute(clubId)
}

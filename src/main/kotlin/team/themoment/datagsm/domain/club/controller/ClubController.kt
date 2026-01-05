package team.themoment.datagsm.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.constant.ClubSortBy
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.service.CreateClubExcelService
import team.themoment.datagsm.domain.club.service.CreateClubService
import team.themoment.datagsm.domain.club.service.DeleteClubService
import team.themoment.datagsm.domain.club.service.ModifyClubExcelService
import team.themoment.datagsm.domain.club.service.ModifyClubService
import team.themoment.datagsm.domain.club.service.QueryClubService
import team.themoment.datagsm.global.common.constant.SortDirection
import team.themoment.datagsm.global.security.annotation.RequireScope

@Tag(name = "Club", description = "동아리 관련 API")
@RestController
@RequestMapping("/v1/clubs")
class ClubController(
    private val queryClubService: QueryClubService,
    private val createClubService: CreateClubService,
    private val modifyClubService: ModifyClubService,
    private val deleteClubService: DeleteClubService,
    private val createClubExcelService: CreateClubExcelService,
    private val modifyClubExcelService: ModifyClubExcelService,
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

    @Operation(summary = "동아리 생성", description = "새로운 동아리를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 동아리", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.CLUB_WRITE)
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
    @RequireScope(ApiScope.CLUB_WRITE)
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
    @RequireScope(ApiScope.CLUB_WRITE)
    @DeleteMapping("/{clubId}")
    fun deleteClub(
        @Parameter(description = "동아리 ID") @PathVariable clubId: Long,
    ) = deleteClubService.execute(clubId)

    @Operation(summary = "동아리 엑셀 생성", description = "저장된 동아리를 바탕으로 엑셀을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
        ],
    )
    @RequireScope(ApiScope.ADMIN_EXCEL)
    @GetMapping("/excel/download")
    fun downloadClubExcel(): ResponseEntity<ByteArray> = createClubExcelService.execute()

    @Operation(summary = "동아리 엑셀 업로드", description = "동아리 이름이 담긴 엑셀을 받아 저장을 진행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "업로드 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (잘못된 셀 값)", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.ADMIN_EXCEL)
    @PostMapping("/excel/upload")
    fun uploadClubExcel(
        @RequestParam("file") file: MultipartFile,
    ) = modifyClubExcelService.execute(file)
}

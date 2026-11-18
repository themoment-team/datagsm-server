package team.themoment.datagsm.web.domain.club.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.request.QueryClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubListResDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.web.domain.club.service.CreateClubExcelService
import team.themoment.datagsm.web.domain.club.service.CreateClubService
import team.themoment.datagsm.web.domain.club.service.DeleteClubService
import team.themoment.datagsm.web.domain.club.service.ModifyClubExcelService
import team.themoment.datagsm.web.domain.club.service.ModifyClubService
import team.themoment.datagsm.web.domain.club.service.QueryClubService

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
    @Operation(
        summary = "동아리 정보 조회",
        description = "필터 조건은 AND로 적용합니다. 동아리 이름은 접두어로 검색하고 해당 페이지 결과가 없으면 부분 일치로 다시 검색합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "관리자 권한 부족", content = [Content()]),
        ],
    )
    @GetMapping
    fun getClubInfo(
        @Valid @ModelAttribute queryReq: QueryClubReqDto,
    ): ClubListResDto = queryClubService.execute(queryReq)

    @Operation(
        summary = "동아리 생성",
        description = "새로운 동아리를 생성합니다. 존재하지 않는 부원 ID는 무시하며 응답에는 계정 연결 여부와 무관하게 조회된 부원을 포함합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "부장으로 지정한 학생을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 동아리 이름", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "관리자 권한 부족", content = [Content()]),
        ],
    )
    @PostMapping
    fun createClub(
        @RequestBody @Valid reqDto: ClubReqDto,
    ): ClubResDto = createClubService.execute(reqDto)

    @Operation(
        summary = "동아리 정보 수정",
        description = "기존 동아리 정보와 소속 학생을 전체 교체합니다. 존재하지 않는 부원 ID는 무시하며 응답에는 계정 연결 여부와 무관하게 조회된 부원을 포함합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리 또는 부장으로 지정한 학생을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 동아리 이름", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "관리자 권한 부족", content = [Content()]),
        ],
    )
    @PutMapping("/{clubId}")
    fun updateClub(
        @Parameter(description = "동아리 ID") @PathVariable clubId: Long,
        @RequestBody @Valid reqDto: ClubReqDto,
    ): ClubResDto = modifyClubService.execute(clubId, reqDto)

    @Operation(
        summary = "동아리 삭제",
        description = "학생의 동아리 참조를 해제한 뒤 기존 동아리를 삭제합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공 (본문 없음)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "관리자 권한 부족", content = [Content()]),
        ],
    )
    @DeleteMapping("/{clubId}")
    fun deleteClub(
        @Parameter(description = "동아리 ID") @PathVariable clubId: Long,
    ) = deleteClubService.execute(clubId)

    @Operation(
        summary = "동아리 엑셀 생성",
        description = "저장된 동아리를 XLSX 파일로 내려받습니다. Content-Disposition: attachment 헤더가 포함됩니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "XLSX 파일 다운로드 성공",
                content = [
                    Content(
                        mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        schema = Schema(type = "string", format = "binary"),
                    ),
                ],
            ),
            ApiResponse(responseCode = "401", description = "로그인 인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "관리자 권한 부족", content = [Content()]),
        ],
    )
    @GetMapping("/exports/excel")
    fun downloadClubExcel(): ResponseEntity<ByteArray> = createClubExcelService.execute()

    @Operation(
        summary = "동아리 엑셀 업로드",
        description =
            "XLSX 또는 XLS 파일의 첫 시트로 동아리 전체를 동기화합니다. 첫 행의 6개 열은 순서대로 동아리명, 동아리종류, 부장, 창설학년도, 운영상태, 폐지학년도여야 " +
                "합니다. 부장은 학번 이름 형식(예: 2404 김태은)입니다. 엑셀에 없는 동아리는 학생 참조를 해제한 뒤 삭제하며 폐지된 동아리도 학생 참조를 해제합니다. " +
                "(ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "업로드 성공"),
            ApiResponse(responseCode = "400", description = "지원하지 않는 확장자, 잘못된 헤더·셀 값·부장 형식, 빈 동아리 목록 또는 중복 이름", content = [Content()]),
            ApiResponse(responseCode = "404", description = "부장의 학번과 이름에 해당하는 학생을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "401", description = "로그인 인증 실패", content = [Content()]),
            ApiResponse(responseCode = "403", description = "관리자 권한 부족", content = [Content()]),
        ],
    )
    @PostMapping("/imports")
    fun uploadClubExcel(
        @Parameter(description = "필수 6열 헤더를 포함한 전체 동아리 XLSX 또는 XLS 파일", required = true)
        @RequestParam("file") file: MultipartFile,
    ) = modifyClubExcelService.execute(file)
}

package team.themoment.datagsm.web.domain.student.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.common.domain.student.dto.request.BatchOperationReqDto
import team.themoment.datagsm.common.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.request.RequestStudentDataEditReqDto
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentStatusReqDto
import team.themoment.datagsm.common.domain.student.dto.response.GraduateStudentResDto
import team.themoment.datagsm.common.domain.student.dto.response.ParticipantCandidateListResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMyGithubIdReqDto
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMySpecialtyReqDto
import team.themoment.datagsm.web.domain.student.service.BatchOperationService
import team.themoment.datagsm.web.domain.student.service.CreateStudentExcelService
import team.themoment.datagsm.web.domain.student.service.CreateStudentService
import team.themoment.datagsm.web.domain.student.service.GraduateStudentService
import team.themoment.datagsm.web.domain.student.service.GraduateThirdGradeStudentsService
import team.themoment.datagsm.web.domain.student.service.ModifyMyGithubIdService
import team.themoment.datagsm.web.domain.student.service.ModifyMySpecialtyService
import team.themoment.datagsm.web.domain.student.service.ModifyStudentExcelService
import team.themoment.datagsm.web.domain.student.service.ModifyStudentService
import team.themoment.datagsm.web.domain.student.service.ModifyStudentStatusService
import team.themoment.datagsm.web.domain.student.service.QueryParticipantCandidateService
import team.themoment.datagsm.web.domain.student.service.QueryStudentService
import team.themoment.datagsm.web.domain.student.service.RequestStudentDataEditService
import team.themoment.datagsm.web.domain.student.service.WithdrawStudentService

@Tag(name = "Student", description = "학생 관련 API")
@RestController
@RequestMapping("/v1/students")
class StudentController(
    private val queryStudentService: QueryStudentService,
    private val createStudentService: CreateStudentService,
    private val modifyStudentService: ModifyStudentService,
    private val createStudentExcelService: CreateStudentExcelService,
    private val modifyStudentExcelService: ModifyStudentExcelService,
    private val graduateStudentService: GraduateStudentService,
    private val graduateThirdGradeStudentsService: GraduateThirdGradeStudentsService,
    private val withdrawStudentService: WithdrawStudentService,
    private val modifyStudentStatusService: ModifyStudentStatusService,
    private val batchOperationService: BatchOperationService,
    private val modifyMySpecialtyService: ModifyMySpecialtyService,
    private val modifyMyGithubIdService: ModifyMyGithubIdService,
    private val requestStudentDataEditService: RequestStudentDataEditService,
    private val queryParticipantCandidateService: QueryParticipantCandidateService,
) {
    @Operation(
        summary = "학생 정보 조회",
        description = "필터 조건에 맞는 학생 정보를 조회합니다. 졸업생·자퇴생 조회에는 해당 include 플래그가 필요하며 onlyEnrolled가 우선합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @GetMapping
    fun getStudentInfo(
        @Valid @ModelAttribute queryReq: QueryStudentReqDto,
    ): StudentListResDto = queryStudentService.execute(queryReq)

    @Operation(
        summary = "프로젝트 참여자 후보 조회",
        description = "프로젝트 신청 시 참여자로 선택할 수 있는 재학생 전체 목록을 조회합니다. 로그인한 사용자라면 누구나 호출할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/participant-candidates")
    fun getParticipantCandidates(): ParticipantCandidateListResDto = queryParticipantCandidateService.execute()

    @Operation(
        summary = "학생 생성",
        description =
            "새 학생을 생성합니다. role 기본값은 GENERAL_STUDENT이며 재학생은 학년·반·번호가 필수입니다. 졸업생은 학번·기숙사·동아리, 자퇴생은 " +
                "기숙사·동아리를 지정할 수 없습니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "400", description = "검증 실패, 재학생 학번 누락 또는 졸업생·자퇴생의 금지된 필드 지정", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 학번", content = [Content()]),
        ],
    )
    @PostMapping
    fun createStudent(
        @RequestBody @Valid reqDto: CreateStudentReqDto,
    ): StudentResDto = createStudentService.execute(reqDto)

    @Operation(
        summary = "학생 정보 수정",
        description = "재학생 정보를 교체하며 생략한 선택 필드는 null로 초기화합니다. 졸업생·자퇴생은 수정할 수 없고 role을 GRADUATE/WITHDRAWN으로 변경할 수도 없습니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "400", description = "검증 실패, 졸업생·자퇴생 수정 또는 해당 역할로 변경 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "학생 또는 동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 학번", content = [Content()]),
        ],
    )
    @PutMapping("/{studentId}")
    fun updateStudent(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
        @RequestBody @Valid reqDto: UpdateStudentReqDto,
    ): StudentResDto = modifyStudentService.execute(studentId, reqDto)

    @Operation(
        summary = "학생 정보 엑셀 생성",
        description = "저장된 학생 정보로 1·2·3학년 시트가 있는 xlsx 파일을 다운로드합니다. includeGraduates=true이면 졸업생 시트를 추가합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(
                responseCode = "200",
                description = "xlsx 파일 다운로드 성공",
                headers = [
                    Header(
                        name = "Content-Disposition",
                        description = "attachment; 학생_현황_yyyyMMdd.xlsx 파일명",
                        schema = Schema(type = "string"),
                    ),
                ],
                content = [
                    Content(
                        mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        schema = Schema(type = "string", format = "binary"),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/exports/excel")
    fun downloadStudentExcel(
        @Parameter(description = "졸업생 포함 여부") @RequestParam(required = false, defaultValue = "false") includeGraduates: Boolean,
    ): ResponseEntity<ByteArray> = createStudentExcelService.execute(includeGraduates)

    @Operation(
        summary = "학생 정보 엑셀 업로드",
        description =
            "기존 학생만 수정하며 신규 학생은 생성하지 않습니다. DB에서 학번이 있는 학생과 엑셀의 학번 집합이 정확히 일치해야 합니다. 전공(specialty)과 " +
                "GitHub 아이디는 변경하지 않습니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "업로드 성공"),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "415", description = "확장자가 xlsx 또는 xls가 아닌 파일", content = [Content()]),
            ApiResponse(responseCode = "400", description = "시트·셀 형식 오류, 학번·이메일 중복, 학번 집합 불일치 또는 존재하지 않는 동아리", content = [Content()]),
        ],
    )
    @PostMapping("/imports")
    fun uploadStudentExcel(
        @Parameter(
            description =
                "xlsx/xls 파일. 시트는 순서대로 1학년·2학년·3학년이며 첫 행은 헤더입니다. 열 순서는 " +
                    "학생명·학번·이메일·학과·전공동아리·창체동아리·호실·소속·성별입니다. 학과는 SW개발과/스마트IoT과/인공지능과, 소속은 " +
                    "일반학생/기숙사자치위원회/학생회/졸업생/자퇴생, 성별은 남자/여자입니다. 동아리와 호실은 비울 수 있습니다.",
        )
        @RequestParam("file") file: MultipartFile,
    ) = modifyStudentExcelService.execute(file)

    @Operation(
        summary = "특정 학생 졸업 처리",
        description = "지정 학생을 졸업생으로 전환하고 학과·전공(specialty)·학번·기숙사·소속 동아리를 초기화하며 동아리 대표 지정도 해제합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "204", description = "졸업 처리 성공", content = [Content()]),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping("/{studentId}/graduate")
    fun graduateStudent(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
    ) {
        graduateStudentService.execute(studentId)
    }

    @Operation(
        summary = "모든 3학년 학생 졸업 처리",
        description = "모든 3학년 학생을 졸업생으로 전환하고 학과·전공(specialty)·학번·기숙사·소속 동아리를 초기화합니다. 동아리 대표 지정은 해제하지 않습니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "졸업 처리 성공"),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
        ],
    )
    @PostMapping("/graduate/third-grade")
    fun graduateThirdGradeStudents(): GraduateStudentResDto = graduateThirdGradeStudentsService.execute()

    @Operation(
        summary = "특정 학생 자퇴 처리",
        description = "지정 학생을 자퇴생으로 전환하고 학과·전공(specialty)·학번·기숙사·소속 동아리를 초기화하며 동아리 대표 지정도 해제합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "204", description = "자퇴 처리 성공", content = [Content()]),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PostMapping("/{studentId}/withdraw")
    fun withdrawStudent(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
    ) {
        withdrawStudentService.execute(studentId)
    }

    @Operation(
        summary = "학생 상태 변경",
        description =
            "학생 역할을 변경합니다. GRADUATE/WITHDRAWN이면 학과·학번·기숙사·소속 동아리를 초기화하고 동아리 대표를 해제하되 " +
                "전공(specialty)은 유지합니다. 재학생 역할로 변경할 때는 역할만 바뀝니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "204", description = "상태 변경 성공", content = [Content()]),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "400", description = "잘못된 요청", content = [Content()]),
            ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{studentId}/status")
    fun updateStudentStatus(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
        @RequestBody @Valid reqDto: UpdateStudentStatusReqDto,
    ) {
        modifyStudentStatusService.execute(studentId, reqDto)
    }

    @Operation(
        summary = "학생 일괄 작업",
        description =
            "GRADUATE 작업만 지원하며 filter 또는 grade를 생략하면 3학년을 처리합니다. 학과·학번·기숙사·소속 동아리를 초기화하지만 " +
                "전공(specialty)과 동아리 대표 지정은 유지합니다. 학년 범위 검증은 없습니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "200", description = "일괄 작업 성공"),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "400", description = "잘못된 요청", content = [Content()]),
        ],
    )
    @PostMapping("/batch-operations")
    fun batchOperation(
        @RequestBody @Valid reqDto: BatchOperationReqDto,
    ): GraduateStudentResDto = batchOperationService.execute(reqDto)

    @Operation(summary = "내 전공 수정", description = "로그인한 학생 본인의 전공을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "204", description = "수정 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 본문 형식 또는 필드 검증 실패", content = [Content()]),
            ApiResponse(responseCode = "404", description = "현재 계정을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않은 계정", content = [Content()]),
        ],
    )
    @PatchMapping("/me/specialty")
    fun updateMySpecialty(
        @RequestBody @Valid reqDto: UpdateMySpecialtyReqDto,
    ) {
        modifyMySpecialtyService.execute(reqDto)
    }

    @Operation(summary = "내 GitHub 아이디 수정", description = "로그인한 학생 본인의 GitHub 아이디를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "204", description = "수정 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 본문 형식 또는 필드 검증 실패", content = [Content()]),
            ApiResponse(responseCode = "404", description = "현재 계정을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "403", description = "학생 정보가 연결되지 않은 계정", content = [Content()]),
        ],
    )
    @PatchMapping("/me/github-id")
    fun updateMyGithubId(
        @RequestBody @Valid reqDto: UpdateMyGithubIdReqDto,
    ) {
        modifyMyGithubIdService.execute(reqDto)
    }

    @Operation(
        summary = "학생 정보 수정 요청 등록",
        description =
            "선택한 학생에게 정보 수정 요청을 등록하고 요청 필드를 즉시 초기화합니다. 커밋 후 비동기로 대상 학생 계정의 SSO 세션과 refresh token을 " +
                "폐기합니다. 이미 발급된 access token은 만료까지 유효합니다. (ADMIN, ROOT 권한 필요)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음", content = [Content()]),
            ApiResponse(responseCode = "204", description = "요청 등록 성공", content = [Content()]),
            ApiResponse(responseCode = "403", description = "ADMIN, ROOT 권한이 없음", content = [Content()]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "존재하지 않는 학생이 포함됨", content = [Content()]),
        ],
    )
    @PostMapping("/data-edit-requests")
    fun requestStudentDataEdit(
        @RequestBody @Valid reqDto: RequestStudentDataEditReqDto,
    ) {
        requestStudentDataEditService.execute(reqDto)
    }
}

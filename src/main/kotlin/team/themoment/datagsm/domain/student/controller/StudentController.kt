package team.themoment.datagsm.domain.student.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
import team.themoment.datagsm.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.service.CreateStudentExcelService
import team.themoment.datagsm.domain.student.service.CreateStudentService
import team.themoment.datagsm.domain.student.service.ModifyStudentExcelService
import team.themoment.datagsm.domain.student.service.ModifyStudentService
import team.themoment.datagsm.domain.student.service.QueryStudentService
import team.themoment.datagsm.global.security.annotation.RequireScope
import java.nio.charset.StandardCharsets

@Tag(name = "Student", description = "학생 관련 API")
@RestController
@RequestMapping("/v1/students")
class StudentController(
    private final val queryStudentService: QueryStudentService,
    private final val createStudentService: CreateStudentService,
    private final val modifyStudentService: ModifyStudentService,
    private final val createStudentExcelService: CreateStudentExcelService,
    private final val modifyStudentExcelService: ModifyStudentExcelService,
) {
    @Operation(summary = "학생 정보 조회", description = "필터 조건에 맞는 학생 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @RequireScope(ApiScope.STUDENT_READ)
    @GetMapping
    fun getStudentInfo(
        @Parameter(description = "학생 ID") @RequestParam(required = false) studentId: Long?,
        @Parameter(description = "이름") @RequestParam(required = false) name: String?,
        @Parameter(description = "이메일") @RequestParam(required = false) email: String?,
        @Parameter(description = "학년 (1-3)") @RequestParam(required = false) grade: Int?,
        @Parameter(description = "반 (1-4)") @RequestParam(required = false) classNum: Int?,
        @Parameter(description = "번호 (1-18)") @RequestParam(required = false) number: Int?,
        @Parameter(description = "성별") @RequestParam(required = false) sex: Sex?,
        @Parameter(description = "역할") @RequestParam(required = false) role: StudentRole?,
        @Parameter(description = "기숙사 호실") @RequestParam(required = false) dormitoryRoom: Int?,
        @Parameter(description = "자퇴 여부") @RequestParam(required = false, defaultValue = "false") isLeaveSchool: Boolean,
        @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "300") size: Int,
    ): StudentListResDto =
        queryStudentService.execute(
            studentId,
            name,
            email,
            grade,
            classNum,
            number,
            sex,
            role,
            dormitoryRoom,
            isLeaveSchool,
            page,
            size,
        )

    @Operation(summary = "학생 생성", description = "새로운 학생 정보를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 학생", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.STUDENT_WRITE)
    @PostMapping
    fun createStudent(
        @RequestBody @Valid reqDto: CreateStudentReqDto,
    ): StudentResDto = createStudentService.execute(reqDto)

    @Operation(summary = "학생 정보 수정", description = "기존 학생의 정보를 전체 교체합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.STUDENT_WRITE)
    @PutMapping("/{studentId}")
    fun updateStudent(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
        @RequestBody @Valid reqDto: UpdateStudentReqDto,
    ): StudentResDto = modifyStudentService.execute(studentId, reqDto)

    @Operation(summary = "학생 정보 엑셀 생성", description = "저장된 학생 정보를 바탕으로 엑셀을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
        ],
    )
    @RequireScope(ApiScope.ADMIN_EXCEL)
    @GetMapping("/excel/download")
    fun downloadStudentExcel(): ResponseEntity<ByteArray> {
        val excelData = createStudentExcelService.execute()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                contentDisposition =
                    ContentDisposition
                        .builder("attachment")
                        .filename("학생정보.xlsx", StandardCharsets.UTF_8)
                        .build()
            }

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(excelData)
    }

    @Operation(summary = "학생 정보 엑셀 업로드", description = "학생 정보가 담긴 엑셀을 받아 수정 또는 저장을 진행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "업로드 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (잘못된 셀 값)", content = [Content()]),
        ],
    )
    @RequireScope(ApiScope.ADMIN_EXCEL)
    @PostMapping("/excel/upload")
    fun uploadStudentExcel(
        @RequestParam("file") file: MultipartFile,
    ) = modifyStudentExcelService.execute(file)
}

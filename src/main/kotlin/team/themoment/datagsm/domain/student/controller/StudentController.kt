package team.themoment.datagsm.domain.student.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.request.StudentCreateReqDto
import team.themoment.datagsm.domain.student.dto.request.StudentUpdateReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.service.CreateStudentService
import team.themoment.datagsm.domain.student.service.ModifyStudentService
import team.themoment.datagsm.domain.student.service.QueryStudentService

@Tag(name = "Student", description = "학생 관련 API")
@RestController
@RequestMapping("/v1/students")
class StudentController(
    private final val queryStudentService: QueryStudentService,
    private final val createStudentService: CreateStudentService,
    private final val modifyStudentService: ModifyStudentService,
) {
    @Operation(summary = "학생 정보 조회", description = "필터 조건에 맞는 학생 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping
    fun getStudentInfo(
        @Parameter(description = "학생 ID") @RequestParam(required = false) studentId: Long?,
        @Parameter(description = "이름") @RequestParam(required = false) name: String?,
        @Parameter(description = "이메일") @RequestParam(required = false) email: String?,
        @Parameter(description = "학년 (1-3)") @RequestParam(required = false) grade: Int?,
        @Parameter(description = "반 (1-4)") @RequestParam(required = false) classNum: Int?,
        @Parameter(description = "번호 (1-18)") @RequestParam(required = false) number: Int?,
        @Parameter(description = "성별") @RequestParam(required = false) sex: Sex?,
        @Parameter(description = "역할") @RequestParam(required = false) role: Role?,
        @Parameter(description = "기숙사 호실") @RequestParam(required = false) dormitoryRoom: Int?,
        @Parameter(description = "자퇴 여부")
        @RequestParam(required = false, defaultValue = "false") isLeaveSchool: Boolean,
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
            ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 학생", content = [Content()]),
        ],
    )
    @PostMapping
    fun createStudent(
        @RequestBody @Valid reqDto: StudentCreateReqDto,
    ): StudentResDto = createStudentService.execute(reqDto)

    @Operation(summary = "학생 정보 수정", description = "기존 학생의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음", content = [Content()]),
        ],
    )
    @PatchMapping("/{studentId}")
    fun updateStudent(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
        @RequestBody @Valid reqDto: StudentUpdateReqDto,
    ): StudentResDto = modifyStudentService.execute(studentId, reqDto)
}

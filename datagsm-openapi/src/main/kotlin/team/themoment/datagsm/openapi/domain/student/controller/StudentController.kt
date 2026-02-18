package team.themoment.datagsm.openapi.domain.student.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.student.dto.request.CreateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.request.QueryStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.request.UpdateStudentReqDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentListResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.openapi.domain.student.service.CreateStudentService
import team.themoment.datagsm.openapi.domain.student.service.ModifyStudentService
import team.themoment.datagsm.openapi.domain.student.service.QueryStudentService
import team.themoment.datagsm.openapi.global.security.annotation.RequireScope

@Tag(name = "Student", description = "학생 관련 API")
@RestController
@RequestMapping("/v1/students")
class StudentController(
    private val queryStudentService: QueryStudentService,
    private val createStudentService: CreateStudentService,
    private val modifyStudentService: ModifyStudentService,
) {
    @Operation(summary = "학생 정보 조회", description = "필터 조건에 맞는 학생 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.STUDENT_READ)
    @GetMapping
    fun getStudentInfo(
        @Valid @ModelAttribute queryReq: QueryStudentReqDto,
    ): StudentListResDto = queryStudentService.execute(queryReq)

    @Operation(summary = "학생 생성", description = "새로운 학생 정보를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 학생", content = [Content()]),
        ],
    )
    @RequireScope(ApiKeyScope.STUDENT_WRITE)
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
    @RequireScope(ApiKeyScope.STUDENT_WRITE)
    @PutMapping("/{studentId}")
    fun updateStudent(
        @Parameter(description = "학생 ID") @PathVariable studentId: Long,
        @RequestBody @Valid reqDto: UpdateStudentReqDto,
    ): StudentResDto = modifyStudentService.execute(studentId, reqDto)
}

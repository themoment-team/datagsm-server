package team.themoment.datagsm.resource.domain.student.controller

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
import team.themoment.datagsm.common.domain.student.Sex
import team.themoment.datagsm.common.domain.student.StudentRole
import team.themoment.datagsm.common.domain.student.StudentSortBy
import team.themoment.datagsm.common.dto.student.response.StudentListResDto
import team.themoment.datagsm.common.global.constant.SortDirection
import team.themoment.datagsm.resource.domain.student.service.QueryStudentService
import team.themoment.datagsm.resource.global.security.annotation.RequireScope

@Tag(name = "Student", description = "학생 관련 API")
@RestController
@RequestMapping("/v1/students")
class StudentController(
    private final val queryStudentService: QueryStudentService,
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
        @Parameter(description = "자퇴 여부") @RequestParam(required = false) isLeaveSchool: Boolean?,
        @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "300") size: Int,
        @Parameter(
            description =
                "정렬 기준 (ID, NAME, EMAIL, STUDENT_NUMBER, GRADE, CLASS_NUM, NUMBER, MAJOR, ROLE, SEX, " +
                    "DORMITORY_ROOM, IS_LEAVE_SCHOOL)",
        ) @RequestParam(required = false) sortBy: StudentSortBy?,
        @Parameter(description = "정렬 방향 (ASC, DESC)") @RequestParam(required = false, defaultValue = "ASC") sortDirection: SortDirection,
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
            sortBy,
            sortDirection,
        )
}

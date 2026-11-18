package team.themoment.datagsm.common.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.entity.constant.StudentSortBy
import team.themoment.datagsm.common.global.constant.SortDirection

data class QueryStudentReqDto(
    @field:Positive
    @param:Schema(description = "학생 ID", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED, minimum = "1")
    val studentId: Long? = null,
    @param:Schema(description = "이름", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val name: String? = null,
    @param:Schema(description = "이메일", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val email: String? = null,
    @field:Min(1)
    @field:Max(3)
    @param:Schema(description = "학년 (1-3)", minimum = "1", maximum = "3", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val grade: Int? = null,
    @field:Min(1)
    @field:Max(4)
    @param:Schema(description = "반 (1-4)", minimum = "1", maximum = "4", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val classNum: Int? = null,
    @field:Min(1)
    @field:Max(18)
    @param:Schema(
        description = "번호 (1-18)",
        minimum = "1",
        maximum = "18",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val number: Int? = null,
    @param:Schema(description = "성별", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val sex: Sex? = null,
    @param:Schema(
        description = "역할 (GRADUATE는 includeGraduates=true, WITHDRAWN은 includeWithdrawn=true 필요; onlyEnrolled=true이면 두 역할 제외)",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val role: StudentRole? = null,
    @param:Schema(description = "기숙사 호실", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val dormitoryRoom: Int? = null,
    @param:Schema(description = "전공", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val specialty: String? = null,
    @param:Schema(description = "학과", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val major: Major? = null,
    @param:Schema(description = "GitHub ID", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val githubId: String? = null,
    @param:Schema(
        description = "졸업생 포함 여부 (role=GRADUATE 조회 시 true 필요, onlyEnrolled=true이면 무시)",
        defaultValue = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val includeGraduates: Boolean = false,
    @param:Schema(
        description = "자퇴생 포함 여부 (role=WITHDRAWN 조회 시 true 필요, onlyEnrolled=true이면 무시)",
        defaultValue = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val includeWithdrawn: Boolean = false,
    @param:Schema(
        description = "재학생만 조회 여부 (true 시 GRADUATE+WITHDRAWN 모두 제외)",
        defaultValue = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val onlyEnrolled: Boolean = false,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(
        description = "페이지 크기",
        defaultValue = "300",
        minimum = "1",
        maximum = "1000",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val size: Int = 300,
    @param:Schema(
        description = "정렬 기준 (ID, NAME, EMAIL, STUDENT_NUMBER, GRADE, CLASS_NUM, NUMBER, MAJOR, ROLE, SEX, DORMITORY_ROOM)",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val sortBy: StudentSortBy? = null,
    @param:Schema(description = "정렬 방향 (ASC, DESC)", defaultValue = "ASC", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val sortDirection: SortDirection = SortDirection.ASC,
)

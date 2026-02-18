package team.themoment.datagsm.common.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.entity.constant.StudentSortBy
import team.themoment.datagsm.common.global.constant.SortDirection

data class QueryStudentReqDto(
    @field:Positive
    @param:Schema(description = "학생 ID")
    val studentId: Long? = null,
    @param:Schema(description = "이름")
    val name: String? = null,
    @param:Schema(description = "이메일")
    val email: String? = null,
    @field:Min(1)
    @field:Max(3)
    @param:Schema(description = "학년 (1-3)", minimum = "1", maximum = "3")
    val grade: Int? = null,
    @field:Min(1)
    @field:Max(4)
    @param:Schema(description = "반 (1-4)", minimum = "1", maximum = "4")
    val classNum: Int? = null,
    @field:Min(1)
    @field:Max(18)
    @param:Schema(description = "번호 (1-18)", minimum = "1", maximum = "18")
    val number: Int? = null,
    @param:Schema(description = "성별")
    val sex: Sex? = null,
    @param:Schema(description = "역할")
    val role: StudentRole? = null,
    @param:Schema(description = "기숙사 호실")
    val dormitoryRoom: Int? = null,
    @param:Schema(description = "졸업생 포함 여부", defaultValue = "false")
    val includeGraduates: Boolean = false,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0")
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(description = "페이지 크기", defaultValue = "300", minimum = "1", maximum = "1000")
    val size: Int = 300,
    @param:Schema(
        description = "정렬 기준 (ID, NAME, EMAIL, STUDENT_NUMBER, GRADE, CLASS_NUM, NUMBER, MAJOR, ROLE, SEX, DORMITORY_ROOM)",
    )
    val sortBy: StudentSortBy? = null,
    @param:Schema(description = "정렬 방향 (ASC, DESC)", defaultValue = "ASC")
    val sortDirection: SortDirection = SortDirection.ASC,
)

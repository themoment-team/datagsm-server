package team.themoment.datagsm.common.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto
import java.time.LocalDateTime

data class AccountResDto(
    @field:Schema(description = "계정 ID", example = "1")
    val id: Long,
    @field:Schema(description = "계정 이메일", example = "user@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "계정 역할 (ROOT, ADMIN, USER)", example = "USER")
    val role: AccountRole,
    @field:Schema(description = "계정 상태 (PENDING, ACTIVE)", example = "ACTIVE")
    val status: AccountStatus,
    @field:Schema(description = "연결 대상 종류 (STUDENT, TEACHER)", example = "STUDENT")
    val objectType: AccountObjectType?,
    @Deprecated("objectType == STUDENT 으로 대체 예정, 하위 호환을 위해 임시 유지", ReplaceWith("objectType == AccountObjectType.STUDENT"))
    @field:Schema(description = "학생 계정 여부 (Deprecated, objectType == STUDENT 으로 대체 예정)", example = "true", deprecated = true)
    val isStudent: Boolean,
    @field:Schema(description = "연결된 학생 정보 (학생 계정인 경우에만 포함)")
    val student: StudentResDto?,
    @field:Schema(description = "연결된 선생님 정보 (선생님 계정인 경우에만 포함)")
    val teacher: TeacherResDto?,
    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime?,
    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(
            account: AccountJpaEntity,
            student: StudentResDto?,
            teacher: TeacherResDto?,
        ): AccountResDto =
            AccountResDto(
                id = account.id!!,
                email = account.email,
                role = account.role,
                status = account.status,
                objectType = account.objectType,
                isStudent = account.objectType == AccountObjectType.STUDENT,
                student = student,
                teacher = teacher,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt,
            )
    }
}

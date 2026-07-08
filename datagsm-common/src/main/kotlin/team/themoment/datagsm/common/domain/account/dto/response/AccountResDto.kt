package team.themoment.datagsm.common.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import java.time.LocalDateTime

data class AccountResDto(
    @field:Schema(description = "계정 ID", example = "1")
    val id: Long,
    @field:Schema(description = "계정 이메일", example = "user@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "계정 역할 (ROOT, ADMIN, USER)", example = "USER")
    val role: AccountRole,
    @field:Schema(description = "학생 계정 여부", example = "true")
    val isStudent: Boolean,
    @field:Schema(description = "연결된 학생 정보 (학생 계정인 경우에만 포함)")
    val student: StudentResDto?,
    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime?,
    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(account: AccountJpaEntity): AccountResDto =
            AccountResDto(
                id = account.id!!,
                email = account.email,
                role = account.role,
                isStudent = account.student != null,
                student = account.student?.let { StudentResDto.from(it) },
                createdAt = account.createdAt,
                updatedAt = account.updatedAt,
            )
    }
}

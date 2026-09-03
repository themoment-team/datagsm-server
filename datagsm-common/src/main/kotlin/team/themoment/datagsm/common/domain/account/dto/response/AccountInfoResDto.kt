package team.themoment.datagsm.common.domain.account.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.dto.internal.ProjectSummaryDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto

data class AccountInfoResDto(
    @field:Schema(description = "계정 ID", example = "1")
    val id: Long,
    @field:Schema(description = "계정 이메일", example = "user@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "계정 역할 (ADMIN, USER)", example = "USER")
    val role: AccountRole,
    @field:Schema(description = "계정 상태 (PENDING, ACTIVE)", example = "ACTIVE")
    val status: AccountStatus,
    @field:Schema(description = "연결 대상 종류 (STUDENT, TEACHER)", example = "STUDENT")
    val objectType: AccountObjectType?,
    @Deprecated("objectType == STUDENT 으로 대체 예정, 하위 호환을 위해 임시 유지", ReplaceWith("objectType == AccountObjectType.STUDENT"))
    @field:Schema(description = "학생 계정 여부 (Deprecated, objectType == STUDENT 으로 대체 예정)", example = "true", deprecated = true)
    val isStudent: Boolean,
    @field:Schema(description = "학생 정보 (student_read 스코프 보유 시에만 포함, OAuth UserInfo 기준)")
    val student: StudentResDto?,
    @field:Schema(description = "선생님 정보 (student_read 스코프 보유 시에만 포함, OAuth UserInfo 기준)")
    val teacher: TeacherResDto?,
    @field:Schema(description = "소속 동아리 목록 (club_read 스코프 보유 시에만 포함, OAuth UserInfo 기준)")
    val clubs: List<ClubSummaryDto> = emptyList(),
    @field:Schema(description = "참여 프로젝트 목록 (project_read 스코프 보유 시에만 포함, OAuth UserInfo 기준)")
    val projects: List<ProjectSummaryDto> = emptyList(),
)

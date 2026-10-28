package team.themoment.datagsm.common.domain.account.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.project.dto.internal.ProjectSummaryDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto

data class AccountInfoResDto(
    // OIDC UserInfo의 필수 클레임. id_token, access token과 같은 값(email)을 쓰며, 내 정보 조회처럼 OIDC가 아닌 응답에서는 생략된다.
    @field:JsonProperty("sub")
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    @field:Schema(description = "OIDC 사용자 식별자 (UserInfo 응답에만 포함, email과 같은 값)", example = "user@gsm.hs.kr")
    val sub: String? = null,
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
    @field:Schema(description = "학생 정보 (학생인 경우에만 포함)")
    val student: StudentResDto?,
    @field:Schema(description = "선생님 정보 (선생님인 경우에만 포함)")
    val teacher: TeacherResDto?,
    @field:Schema(description = "소속 동아리 목록")
    val clubs: List<ClubSummaryDto> = emptyList(),
    @field:Schema(description = "참여 프로젝트 목록")
    val projects: List<ProjectSummaryDto> = emptyList(),
)

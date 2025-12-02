package team.themoment.datagsm.domain.student.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole

data class UpdateStudentReqDto(
    @field:Size(max = 30)
    @param:Schema(description = "이름", example = "홍길동", maxLength = 30)
    val name: String?,
    @param:Schema(description = "성별", example = "WOMAN")
    val sex: Sex?,
    @field:Size(max = 50)
    @field:Email
    @param:Schema(description = "이메일", example = "student@gsm.hs.kr", maxLength = 50)
    val email: String?,
    @field:Min(value = 1)
    @field:Max(value = 3)
    @param:Schema(description = "학년 (1-3)", example = "1", minimum = "1", maximum = "3")
    val grade: Int?,
    @field:Min(value = 1)
    @field:Max(value = 4)
    @param:Schema(description = "반 (1-4)", example = "1", minimum = "1", maximum = "4")
    val classNum: Int?,
    @field:Min(value = 1)
    @field:Max(value = 18)
    @param:Schema(description = "번호 (1-18)", example = "1", minimum = "1", maximum = "18")
    val number: Int?,
    @param:Schema(description = "역할", example = "GENERAL_STUDENT")
    val role: StudentRole?,
    @field:Min(value = 201)
    @field:Max(value = 518)
    @param:Schema(description = "기숙사 호실 (201-518)", example = "301", minimum = "201", maximum = "518")
    val dormitoryRoomNumber: Int?,
    @param:Schema(description = "전공 동아리 ID", example = "1")
    val majorClubId: Long? = null,
    @param:Schema(description = "취업 동아리 ID", example = "2")
    val jobClubId: Long? = null,
    @param:Schema(description = "자율 동아리 ID", example = "3")
    val autonomousClubId: Long? = null,
)

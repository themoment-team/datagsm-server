package team.themoment.datagsm.domain.student.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole

data class ExcelColumnDto(
    @field:NotBlank
    @field:Size(max = 50)
    @param:Schema(description = "학생 성명", example = "홍길동", maxLength = 50)
    val name: String,
    @field:Min(1101)
    @field:Max(3418)
    @param:Schema(description = "학번", example = "1101", minimum = "1101", maximum = "3418")
    val number: Int,
    @field:Size(max = 50)
    @field:NotBlank
    @field:Email
    @param:Schema(description = "이메일", example = "student@gsm.hs.kr", maxLength = 50)
    val email: String,
    @field:NotNull
    @param:Schema(description = "학과", example = "SW개발과")
    val major: Major,
    @field:Size(max = 50)
    @param:Schema(description = "전공동아리", example = "더모먼트", maxLength = 50)
    val majorClub: String?,
    @field:Size(max = 50)
    @param:Schema(description = "취업동아리", example = "백엔드2", maxLength = 50)
    val jobClub: String?,
    @field:Size(max = 50)
    @param:Schema(description = "창체동아리", example = "블렌드", maxLength = 50)
    val autonomousClub: String?,
    @field:Min(201)
    @field:Max(518)
    @param:Schema(description = "기숙사 번호", example = "201", minimum = "201", maximum = "518")
    val dormitoryRoomNumber: Int?,
    @field:NotNull
    @param:Schema(description = "소속", example = "학생회")
    val role: StudentRole,
    @param:Schema(description = "자퇴 여부", example = "true")
    val isLeaveSchool: Boolean,
    @field:NotNull
    @param:Schema(description = "성별", example = "남")
    val sex: Sex,
)

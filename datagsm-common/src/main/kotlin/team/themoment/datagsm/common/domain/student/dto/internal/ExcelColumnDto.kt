package team.themoment.datagsm.common.domain.student.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole

data class ExcelColumnDto(
    @field:Schema(description = "학생 성명", example = "홍길동", maxLength = 50)
    val name: String,
    @field:Schema(description = "학번", example = "1101", minimum = "1101", maximum = "3418")
    val number: Int?,
    @field:Schema(description = "이메일", example = "student@gsm.hs.kr", maxLength = 50)
    val email: String,
    @field:Schema(description = "학과", example = "SW개발과")
    val major: Major?,
    @field:Schema(description = "전공동아리", example = "더모먼트", maxLength = 50)
    val majorClub: String?,
    @field:Schema(description = "취업동아리", example = "백엔드2", maxLength = 50)
    val jobClub: String?,
    @field:Schema(description = "창체동아리", example = "블렌드", maxLength = 50)
    val autonomousClub: String?,
    @field:Schema(description = "기숙사 번호", example = "201", minimum = "201", maximum = "518")
    val dormitoryRoomNumber: Int?,
    @field:Schema(description = "소속", example = "학생회")
    val role: StudentRole,
    @field:Schema(description = "성별", example = "남")
    val sex: Sex,
)

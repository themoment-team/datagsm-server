package team.themoment.datagsm.common.domain.student.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex

data class ParticipantInfoDto(
    @field:Schema(description = "학생 ID", example = "1")
    val id: Long,
    @field:Schema(description = "학생 이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "학생 이메일", example = "s24080@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "학번", example = "1201")
    val studentNumber: Int?,
    @field:Schema(description = "학과", example = "SW_DEVELOPMENT", allowableValues = ["SW_DEVELOPMENT", "SMART_IOT", "AI"])
    val major: Major?,
    @field:Schema(description = "성별", example = "MAN")
    val sex: Sex,
)

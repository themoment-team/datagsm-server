package team.themoment.datagsm.web.domain.student.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.Major
import team.themoment.datagsm.common.domain.student.Sex

data class ParticipantInfoDto(
    @param:Schema(description = "학생 ID", example = "1")
    val id: Long,
    @param:Schema(description = "학생 이름", example = "홍길동")
    val name: String,
    @param:Schema(description = "학생 이메일", example = "s24080@gsm.hs.kr")
    val email: String,
    @param:Schema(description = "학번", example = "1201")
    val studentNumber: Int,
    @param:Schema(description = "학과", example = "SW_DEVELOPMENT", allowableValues = ["SW_DEVELOPMENT", "SMART_IOT", "AI"])
    val major: Major,
    @param:Schema(description = "성별", example = "MAN")
    val sex: Sex,
)

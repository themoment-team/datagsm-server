package team.themoment.datagsm.common.domain.club.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto

data class ClubResDto(
    @field:Schema(description = "동아리 ID", example = "1")
    val id: Long,
    @field:Schema(description = "동아리 이름", example = "SW개발동아리")
    val name: String,
    @field:Schema(description = "동아리 종류", example = "MAJOR_CLUB")
    val type: ClubType,
    @field:Schema(description = "동아리 부장")
    val leader: ParticipantInfoDto?,
    @field:Schema(description = "동아리 부원 목록")
    val participants: List<ParticipantInfoDto>,
    @field:Schema(description = "창설 학년도", example = "2022")
    val foundedYear: Int,
    @field:Schema(description = "운영 상태", example = "ACTIVE")
    val status: ClubStatus,
    @field:Schema(description = "폐지 학년도", example = "2024")
    val abolishedYear: Int?,
)

package team.themoment.datagsm.common.domain.club.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType

data class ClubReqDto(
    @field:NotBlank
    @field:Size(max = 30)
    @param:Schema(description = "동아리 이름", example = "SW개발동아리", maxLength = 30)
    val name: String,
    @param:Schema(description = "동아리 종류", example = "MAJOR_CLUB")
    val type: ClubType,
    @param:Schema(description = "동아리 부장 학생 ID (ABOLISHED 시 null)", example = "1")
    val leaderId: Long?,
    @field:Size(max = 100, message = "부원은 최대 100명 이하여야 합니다")
    @param:Schema(description = "동아리 부원 학생 ID 목록 (ABOLISHED 시 빈 배열)", example = "[2, 3, 4]")
    val participantIds: List<Long>,
    @field:Positive
    @param:Schema(description = "창설 학년도", example = "2022")
    val foundedYear: Int,
    @param:Schema(description = "운영 상태", example = "ACTIVE")
    val status: ClubStatus,
    @field:Positive
    @param:Schema(description = "폐지 학년도 (ABOLISHED 시 설정)", example = "2024")
    val abolishedYear: Int? = null,
)

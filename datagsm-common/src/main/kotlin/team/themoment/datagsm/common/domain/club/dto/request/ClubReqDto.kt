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
    @param:Schema(description = "동아리 이름", example = "SW개발동아리", maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String,
    @param:Schema(description = "동아리 종류", example = "MAJOR_CLUB", requiredMode = Schema.RequiredMode.REQUIRED)
    val type: ClubType,
    @param:Schema(description = "동아리 부장 학생 ID (ABOLISHED 시 null, ACTIVE 시 부장 또는 부원 ID가 최소 하나 필요)", example = "1", nullable = true)
    val leaderId: Long?,
    @field:Size(max = 100, message = "부원은 최대 100명 이하여야 합니다")
    @param:Schema(
        description = "동아리 부원 학생 ID 목록 (최대 100명, ABOLISHED 시 빈 배열, 존재하지 않는 ID 및 부장과 같은 ID는 무시)",
        example = "[2, 3, 4]",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val participantIds: List<Long>,
    @field:Positive
    @param:Schema(description = "창설 학년도", example = "2022", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val foundedYear: Int,
    @param:Schema(description = "운영 상태", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    val status: ClubStatus,
    @field:Positive
    @param:Schema(
        description = "폐지 학년도 (ABOLISHED여도 null 허용, ACTIVE이면 입력값을 무시하고 null로 저장)",
        example = "2024",
        minimum = "1",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val abolishedYear: Int? = null,
)

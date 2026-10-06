package team.themoment.datagsm.common.domain.club.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType

data class QueryPublicClubReqDto(
    @param:Schema(description = "동아리 종류 (미입력 시 전체 조회)")
    val clubType: ClubType? = null,
    @param:Schema(description = "운영 상태 (미입력 시 전체 조회)")
    val clubStatus: ClubStatus? = null,
)

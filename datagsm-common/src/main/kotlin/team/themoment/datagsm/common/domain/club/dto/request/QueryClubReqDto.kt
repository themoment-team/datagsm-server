package team.themoment.datagsm.common.domain.club.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import team.themoment.datagsm.common.domain.club.entity.constant.ClubSortBy
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.global.constant.SortDirection

data class QueryClubReqDto(
    @field:Positive
    @param:Schema(description = "동아리 ID")
    val clubId: Long? = null,
    @param:Schema(description = "동아리 이름")
    val clubName: String? = null,
    @param:Schema(description = "동아리 종류")
    val clubType: ClubType? = null,
    @param:Schema(description = "운영 상태")
    val clubStatus: ClubStatus? = null,
    @field:Positive
    @param:Schema(description = "창설 학년도")
    val foundedYear: Int? = null,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", defaultValue = "0", minimum = "0")
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    @param:Schema(description = "페이지 크기", defaultValue = "100", minimum = "1", maximum = "1000")
    val size: Int = 100,
    @param:Schema(description = "부장을 부원 목록에 포함할지 여부", defaultValue = "false")
    val includeLeaderInParticipants: Boolean = false,
    @param:Schema(description = "정렬 기준 (ID, NAME, TYPE, FOUNDED_YEAR, STATUS)")
    val sortBy: ClubSortBy? = null,
    @param:Schema(description = "정렬 방향 (ASC, DESC)", defaultValue = "ASC")
    val sortDirection: SortDirection = SortDirection.ASC,
)

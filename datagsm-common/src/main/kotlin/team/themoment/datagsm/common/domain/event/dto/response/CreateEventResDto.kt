package team.themoment.datagsm.common.domain.event.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.event.entity.constant.EventVerificationStatus
import java.time.LocalDateTime

data class CreateEventResDto(
    @field:Schema(description = "Event ID")
    @field:JsonProperty("id")
    val id: Long,
    @field:Schema(description = "Event 수신 URL")
    @field:JsonProperty("target_url")
    val targetUrl: String,
    @field:Schema(description = "구독 이벤트 목록")
    @field:JsonProperty("events")
    val events: Set<EventType>,
    @field:Schema(description = "활성화 여부")
    @field:JsonProperty("is_active")
    val isActive: Boolean,
    @field:Schema(description = "생성 일시")
    @field:JsonProperty("created_at")
    val createdAt: LocalDateTime,
    @field:Schema(description = "URL 검증 상태")
    @field:JsonProperty("verification_status")
    val verificationStatus: EventVerificationStatus,
    @field:Schema(description = "서명 검증용 secret (이 응답에서만 노출됩니다)")
    @field:JsonProperty("secret")
    val secret: String,
)

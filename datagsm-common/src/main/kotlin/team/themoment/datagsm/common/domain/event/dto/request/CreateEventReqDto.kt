package team.themoment.datagsm.common.domain.event.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import team.themoment.datagsm.common.domain.event.entity.constant.EventType

data class CreateEventReqDto(
    @param:Schema(description = "Event 수신 URL")
    @field:NotBlank
    @field:Pattern(regexp = "^https?://.*", message = "URL은 http:// 또는 https://로 시작해야 합니다.")
    @field:JsonProperty("target_url")
    val targetUrl: String,
    @param:Schema(description = "구독할 이벤트 목록")
    @field:NotEmpty(message = "구독할 이벤트를 하나 이상 선택해야 합니다.")
    @field:JsonProperty("events")
    val events: Set<EventType>,
)

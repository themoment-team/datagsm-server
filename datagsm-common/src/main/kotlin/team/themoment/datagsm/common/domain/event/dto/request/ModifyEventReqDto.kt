package team.themoment.datagsm.common.domain.event.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import team.themoment.datagsm.common.domain.event.entity.constant.EventType

data class ModifyEventReqDto(
    @param:Schema(description = "변경할 Event 수신 URL (null이면 변경 없음)")
    @field:Pattern(regexp = "^https?://.*", message = "URL은 http:// 또는 https://로 시작해야 합니다.")
    @field:JsonProperty("target_url")
    val targetUrl: String?,
    @param:Schema(description = "변경할 구독 이벤트 목록 (null이면 변경 없음, 빈 배열 불가)")
    @field:Size(min = 1, message = "구독할 이벤트를 하나 이상 선택해야 합니다.")
    @field:JsonProperty("events")
    val events: Set<EventType>?,
)

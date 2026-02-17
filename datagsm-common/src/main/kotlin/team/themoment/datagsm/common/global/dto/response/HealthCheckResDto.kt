package team.themoment.datagsm.common.global.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

// TODO: DTO 쪼개기
data class HealthCheckResDto(
    @field:Schema(description = "서버 상태", example = "OK")
    @field:JsonProperty("status")
    val status: HealthStatus,
    @field:Schema(description = "응답 시간 메트릭")
    @field:JsonProperty("metrics")
    val metrics: MetricsDto? = null,
    @field:Schema(description = "상태 메시지", example = "서버가 정상적으로 작동 중입니다.")
    @field:JsonProperty("message")
    val message: String,
)

enum class HealthStatus {
    @JsonProperty("OK")
    OK,

    @JsonProperty("DEGRADED")
    DEGRADED,

    @JsonProperty("DOWN")
    DOWN,
}

data class MetricsDto(
    @field:Schema(description = "샘플 크기 (수집된 요청 수)", example = "1000")
    @field:JsonProperty("sample_size")
    val sampleSize: Int,
    @field:Schema(description = "평균 응답 시간 (밀리초)", example = "245.3")
    @field:JsonProperty("avg_ms")
    val avgMs: Double,
    @field:Schema(description = "P50 응답 시간 (밀리초)", example = "180")
    @field:JsonProperty("p50_ms")
    val p50Ms: Long,
    @field:Schema(description = "P95 응답 시간 (밀리초)", example = "450")
    @field:JsonProperty("p95_ms")
    val p95Ms: Long,
    @field:Schema(description = "P99 응답 시간 (밀리초)", example = "720")
    @field:JsonProperty("p99_ms")
    val p99Ms: Long,
    @field:Schema(description = "에러율 (5xx 응답 비율)", example = "0.01")
    @field:JsonProperty("error_rate")
    val errorRate: Double,
)

package team.themoment.datagsm.common.global.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.global.data.HealthCheckEnvironment
import team.themoment.datagsm.common.global.dto.internal.HealthCheckMetricsResult
import team.themoment.datagsm.common.global.dto.response.HealthCheckResDto
import team.themoment.datagsm.common.global.dto.response.HealthStatus
import team.themoment.datagsm.common.global.dto.response.MetricsDto
import team.themoment.datagsm.common.global.metrics.ResponseTimeMetricsCollector

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/v1/health")
class HealthController(
    private val metricsCollector: ResponseTimeMetricsCollector,
    private val healthCheckEnvironment: HealthCheckEnvironment,
) {
    @Operation(
        summary = "서버 상태 확인",
        description = "서버가 정상적으로 작동하는지 확인합니다. 응답 시간 메트릭을 기반으로 상태를 판단합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "서버 상태 (OK/DEGRADED/DOWN)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HealthCheckResDto::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping
    fun checkHealth(): HealthCheckResDto {
        if (!healthCheckEnvironment.metrics.enabled) {
            return HealthCheckResDto(
                status = HealthStatus.OK,
                metrics = null,
                message = "서버가 정상적으로 작동 중입니다.",
            )
        }

        val metrics = metricsCollector.getMetrics()
        val status = determineHealthStatus(metrics)
        val message = generateStatusMessage(status, metrics)

        return HealthCheckResDto(
            status = status,
            metrics =
                MetricsDto(
                    sampleSize = metrics.sampleSize,
                    avgMs = String.format("%.1f", metrics.avgMs).toDouble(),
                    p50Ms = metrics.p50Ms,
                    p95Ms = metrics.p95Ms,
                    p99Ms = metrics.p99Ms,
                    errorRate = String.format("%.2f", metrics.errorRate).toDouble(),
                ),
            message = message,
        )
    }

    private fun determineHealthStatus(metrics: HealthCheckMetricsResult): HealthStatus {
        if (metrics.sampleSize == 0) {
            return HealthStatus.OK
        }

        val thresholds = healthCheckEnvironment.thresholds

        if (metrics.p95Ms >= thresholds.criticalP95Ms ||
            metrics.avgMs >= thresholds.criticalAvgMs ||
            metrics.errorRate >= thresholds.errorRateCritical
        ) {
            return HealthStatus.DOWN
        }

        if (metrics.p95Ms >= thresholds.warnP95Ms ||
            metrics.avgMs >= thresholds.warnAvgMs ||
            metrics.errorRate >= thresholds.errorRateWarn
        ) {
            return HealthStatus.DEGRADED
        }

        return HealthStatus.OK
    }

    private fun generateStatusMessage(
        status: HealthStatus,
        metrics: HealthCheckMetricsResult,
    ): String =
        when (status) {
            HealthStatus.OK -> "서버가 정상적으로 작동 중입니다."
            HealthStatus.DEGRADED ->
                "서버 응답 속도가 저하되었습니다. (P95: ${metrics.p95Ms}ms, Avg: ${String.format("%.0f", metrics.avgMs)}ms)"
            HealthStatus.DOWN -> {
                val errorRatePercent = (metrics.errorRate * 100).toInt()
                "서버가 과부하 상태입니다. (P95: ${metrics.p95Ms}ms, Avg: ${String.format("%.0f", metrics.avgMs)}ms, ErrorRate: $errorRatePercent%)"
            }
        }
}

package team.themoment.datagsm.common.global.metrics

import team.themoment.datagsm.common.global.dto.internal.HealthCheckMetricsResult

interface ResponseTimeMetricsCollector {
    fun recordResponseTime(
        durationMs: Long,
        statusCode: Int,
    )

    fun getMetrics(): HealthCheckMetricsResult
}

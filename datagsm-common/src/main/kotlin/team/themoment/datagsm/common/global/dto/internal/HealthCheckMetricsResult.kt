package team.themoment.datagsm.common.global.dto.internal

data class HealthCheckMetricsResult(
    val sampleSize: Int,
    val avgMs: Double,
    val p50Ms: Long,
    val p95Ms: Long,
    val p99Ms: Long,
    val errorRate: Double,
)

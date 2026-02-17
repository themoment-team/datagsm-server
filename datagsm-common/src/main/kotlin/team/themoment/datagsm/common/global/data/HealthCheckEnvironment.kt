package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.health-check")
data class HealthCheckEnvironment(
    val metrics: Metrics,
    val thresholds: Thresholds,
    val excludedPaths: List<String> = listOf("/v1/health"),
) {
    data class Metrics(
        val enabled: Boolean,
        val sampleSize: Int = 1000,
    )

    data class Thresholds(
        val warnP95Ms: Long,
        val criticalP95Ms: Long,
        val warnAvgMs: Long,
        val criticalAvgMs: Long,
        val errorRateWarn: Double = 0.05,
        val errorRateCritical: Double = 0.10,
    )
}

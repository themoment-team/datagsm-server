package team.themoment.datagsm.common.global.metrics

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.global.data.HealthCheckEnvironment
import team.themoment.datagsm.common.global.dto.internal.HealthCheckMetricsResult
import java.util.concurrent.atomic.AtomicReference

@Component
class InMemoryResponseTimeMetrics(
    private val healthCheckEnvironment: HealthCheckEnvironment,
) : ResponseTimeMetricsCollector {
    private val buffer = AtomicReference(CircularBuffer(healthCheckEnvironment.metrics.sampleSize))

    override fun recordResponseTime(
        durationMs: Long,
        statusCode: Int,
    ) {
        if (!healthCheckEnvironment.metrics.enabled) return

        buffer.updateAndGet { currentBuffer ->
            currentBuffer.add(
                MetricEntry(
                    responseTimeMs = durationMs,
                    isError = statusCode >= 500,
                ),
            )
        }
    }

    override fun getMetrics(): HealthCheckMetricsResult {
        val currentBuffer = buffer.get()
        val entries = currentBuffer.getAll()

        if (entries.isEmpty()) {
            return HealthCheckMetricsResult(
                sampleSize = 0,
                avgMs = 0.0,
                p50Ms = 0,
                p95Ms = 0,
                p99Ms = 0,
                errorRate = 0.0,
            )
        }

        val responseTimes = entries.map { it.responseTimeMs }.sorted()
        val errorCount = entries.count { it.isError }

        return HealthCheckMetricsResult(
            sampleSize = entries.size,
            avgMs = responseTimes.average(),
            p50Ms = calculatePercentile(responseTimes, 50.0),
            p95Ms = calculatePercentile(responseTimes, 95.0),
            p99Ms = calculatePercentile(responseTimes, 99.0),
            errorRate = errorCount.toDouble() / entries.size,
        )
    }

    private fun calculatePercentile(
        sortedValues: List<Long>,
        percentile: Double,
    ): Long {
        if (sortedValues.isEmpty()) return 0

        val index = ((percentile / 100.0) * (sortedValues.size - 1)).toInt()
        return sortedValues[index.coerceIn(0, sortedValues.size - 1)]
    }

    data class MetricEntry(
        val responseTimeMs: Long,
        val isError: Boolean,
    )

    private data class CircularBuffer(
        val capacity: Int,
        private val entries: List<MetricEntry> = emptyList(),
    ) {
        fun add(entry: MetricEntry): CircularBuffer {
            val newEntries =
                if (entries.size >= capacity) {
                    entries.drop(1) + entry
                } else {
                    entries + entry
                }
            return copy(entries = newEntries)
        }

        fun getAll(): List<MetricEntry> = entries
    }
}

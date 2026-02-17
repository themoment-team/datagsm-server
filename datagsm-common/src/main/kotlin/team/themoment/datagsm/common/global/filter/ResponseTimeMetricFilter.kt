package team.themoment.datagsm.common.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.global.data.HealthCheckEnvironment
import team.themoment.datagsm.common.global.metrics.ResponseTimeMetricsCollector

class ResponseTimeMetricFilter(
    private val metricsCollector: ResponseTimeMetricsCollector,
    private val healthCheckEnvironment: HealthCheckEnvironment,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!healthCheckEnvironment.metrics.enabled) {
            filterChain.doFilter(request, response)
            return
        }

        if (shouldExclude(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val startTime = System.nanoTime()

        try {
            filterChain.doFilter(request, response)
        } finally {
            val endTime = System.nanoTime()
            val durationMs = (endTime - startTime) / 1_000_000

            metricsCollector.recordResponseTime(
                durationMs = durationMs,
                statusCode = response.status,
            )
        }
    }

    private fun shouldExclude(requestUri: String): Boolean =
        healthCheckEnvironment.excludedPaths.any { pattern ->
            pathMatcher.match(pattern, requestUri)
        }
}

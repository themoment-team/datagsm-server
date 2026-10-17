package team.themoment.datagsm.web.global.security.service.impl

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.global.data.PublicApiRateLimitEnvironment
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.web.global.security.service.PublicApiRateLimitService
import java.time.Duration

@Service
class PublicApiRateLimitServiceImpl(
    private val proxyManager: ProxyManager<String>,
    private val publicApiRateLimitEnvironment: PublicApiRateLimitEnvironment,
) : PublicApiRateLimitService {
    override fun tryConsumeAndReturnRemaining(clientIp: String): RateLimitConsumeResult {
        if (!publicApiRateLimitEnvironment.enabled) {
            return RateLimitConsumeResult(
                consumed = true,
                remainingTokens = publicApiRateLimitEnvironment.capacity,
                secondsToWaitForRefill = 0,
            )
        }

        val bucket =
            proxyManager.builder().build(
                "rate_limit:public_api:$clientIp",
            ) { createBucketConfiguration() }
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        return RateLimitConsumeResult(
            consumed = probe.isConsumed,
            remainingTokens = probe.remainingTokens,
            secondsToWaitForRefill = probe.nanosToWaitForRefill / 1_000_000_000,
        )
    }

    private fun createBucketConfiguration(): BucketConfiguration {
        val bandwidth =
            Bandwidth
                .builder()
                .capacity(publicApiRateLimitEnvironment.capacity)
                .refillIntervally(
                    publicApiRateLimitEnvironment.refillTokens,
                    Duration.ofSeconds(publicApiRateLimitEnvironment.refillDurationSeconds),
                ).build()
        return BucketConfiguration
            .builder()
            .addLimit(bandwidth)
            .build()
    }
}

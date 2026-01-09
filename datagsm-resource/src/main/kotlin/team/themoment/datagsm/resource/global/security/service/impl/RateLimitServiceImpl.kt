package team.themoment.datagsm.resource.global.security.service.impl

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.resource.global.security.service.RateLimitService
import java.time.Duration

@Service
class RateLimitServiceImpl(
    private val proxyManager: ProxyManager<String>,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : RateLimitService {
    override fun tryConsume(apiKey: ApiKey): Boolean {
        if (!apiKeyEnvironment.rateLimit.enabled) return true

        val bucket = getBucket(apiKey)
        return bucket.tryConsume(1)
    }

    override fun getRemainingTokens(apiKey: ApiKey): Long {
        val bucket = getBucket(apiKey)
        return bucket.availableTokens
    }

    override fun getSecondsUntilRefill(apiKey: ApiKey): Long {
        val bucket = getBucket(apiKey)
        val probe = bucket.estimateAbilityToConsume(1)
        return if (probe.canBeConsumed()) 0 else nanosToSeconds(probe.nanosToWaitForRefill)
    }

    override fun tryConsumeAndReturnRemaining(apiKey: ApiKey): RateLimitConsumeResult {
        if (!apiKeyEnvironment.rateLimit.enabled) {
            return RateLimitConsumeResult(
                consumed = true,
                remainingTokens = apiKey.rateLimitCapacity,
                secondsToWaitForRefill = 0,
            )
        }

        val bucket = getBucket(apiKey)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        return RateLimitConsumeResult(
            consumed = probe.isConsumed,
            remainingTokens = probe.remainingTokens,
            secondsToWaitForRefill = nanosToSeconds(probe.nanosToWaitForRefill),
        )
    }

    private fun getBucket(apiKey: ApiKey) =
        proxyManager.builder().build(
            "rate_limit:api_key:${apiKey.value}",
            { createBucketConfiguration(apiKey) },
        )

    private fun createBucketConfiguration(apiKey: ApiKey): BucketConfiguration {
        val bandwidth =
            Bandwidth
                .builder()
                .capacity(apiKey.rateLimitCapacity)
                .refillGreedy(
                    apiKey.rateLimitRefillTokens,
                    Duration.ofSeconds(apiKey.rateLimitRefillDurationSeconds),
                ).build()

        return BucketConfiguration
            .builder()
            .addLimit(bandwidth)
            .build()
    }

    private fun nanosToSeconds(nanos: Long) = nanos / 1_000_000_000
}

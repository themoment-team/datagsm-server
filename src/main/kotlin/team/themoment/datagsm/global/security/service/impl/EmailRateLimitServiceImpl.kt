package team.themoment.datagsm.global.security.service.impl

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import team.themoment.datagsm.global.security.annotation.EmailRateLimitType
import team.themoment.datagsm.global.security.data.EmailRateLimitEnvironment
import team.themoment.datagsm.global.security.dto.RateLimitConsumeResult
import team.themoment.datagsm.global.security.service.EmailRateLimitService
import java.time.Duration

@Service
class EmailRateLimitServiceImpl(
    private val proxyManager: ProxyManager<String>,
    private val emailRateLimitEnvironment: EmailRateLimitEnvironment,
) : EmailRateLimitService {
    companion object {
        private const val SEND_EMAIL_BUCKET_PREFIX = "rate_limit:email:send:"
        private const val CHECK_EMAIL_BUCKET_PREFIX = "rate_limit:email:check:"
    }

    override fun tryConsume(
        email: String,
        type: EmailRateLimitType,
    ): RateLimitConsumeResult {
        val config = getConfig(type)

        if (!emailRateLimitEnvironment.enabled) {
            return createDisabledResult(config.capacity)
        }

        val bucket =
            proxyManager.builder().build(
                "${getBucketPrefix(type)}$email",
                { createBucketConfiguration(config) },
            )
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        return RateLimitConsumeResult(
            consumed = probe.isConsumed,
            remainingTokens = probe.remainingTokens,
            secondsToWaitForRefill = nanosToSeconds(probe.nanosToWaitForRefill),
        )
    }

    private fun getConfig(type: EmailRateLimitType): EmailRateLimitEnvironment.RateLimitConfig =
        when (type) {
            EmailRateLimitType.SEND_EMAIL -> emailRateLimitEnvironment.send
            EmailRateLimitType.CHECK_EMAIL -> emailRateLimitEnvironment.check
        }

    private fun getBucketPrefix(type: EmailRateLimitType): String =
        when (type) {
            EmailRateLimitType.SEND_EMAIL -> SEND_EMAIL_BUCKET_PREFIX
            EmailRateLimitType.CHECK_EMAIL -> CHECK_EMAIL_BUCKET_PREFIX
        }

    private fun createDisabledResult(capacity: Long) =
        RateLimitConsumeResult(
            consumed = true,
            remainingTokens = capacity,
            secondsToWaitForRefill = 0,
        )

    private fun nanosToSeconds(nanos: Long): Long = nanos / 1_000_000_000

    private fun createBucketConfiguration(config: EmailRateLimitEnvironment.RateLimitConfig): BucketConfiguration {
        val bandwidth =
            Bandwidth
                .builder()
                .capacity(config.capacity)
                .refillGreedy(config.refillTokens, Duration.ofMinutes(config.refillDurationMinutes))
                .build()
        return BucketConfiguration
            .builder()
            .addLimit(bandwidth)
            .build()
    }
}

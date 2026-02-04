package team.themoment.datagsm.oauth.authorization.global.security.service.impl

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.global.data.PasswordResetRateLimitEnvironment
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.global.security.annotation.PasswordResetRateLimitType
import team.themoment.datagsm.oauth.authorization.global.security.service.PasswordResetRateLimitService
import java.time.Duration

@Service
class PasswordResetRateLimitServiceImpl(
    private val proxyManager: ProxyManager<String>,
    private val passwordResetRateLimitEnvironment: PasswordResetRateLimitEnvironment,
) : PasswordResetRateLimitService {
    companion object {
        private const val SEND_EMAIL_BUCKET_PREFIX = "rate_limit:password_reset:send:"
        private const val CHECK_CODE_BUCKET_PREFIX = "rate_limit:password_reset:check:"
        private const val MODIFY_PASSWORD_BUCKET_PREFIX = "rate_limit:password_reset:modify:"
    }

    override fun tryConsume(
        email: String,
        type: PasswordResetRateLimitType,
    ): RateLimitConsumeResult {
        val config = getConfig(type)

        if (!passwordResetRateLimitEnvironment.enabled) {
            return createDisabledResult(config.capacity)
        }

        val bucket =
            proxyManager.builder().build(
                "${getBucketPrefix(type)}$email",
            ) { createBucketConfiguration(config) }
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        return RateLimitConsumeResult(
            consumed = probe.isConsumed,
            remainingTokens = probe.remainingTokens,
            secondsToWaitForRefill = nanosToSeconds(probe.nanosToWaitForRefill),
        )
    }

    private fun getConfig(type: PasswordResetRateLimitType): PasswordResetRateLimitEnvironment.RateLimitConfig =
        when (type) {
            PasswordResetRateLimitType.SEND_EMAIL -> passwordResetRateLimitEnvironment.send
            PasswordResetRateLimitType.CHECK_CODE -> passwordResetRateLimitEnvironment.verify
            PasswordResetRateLimitType.MODIFY_PASSWORD -> passwordResetRateLimitEnvironment.change
        }

    private fun getBucketPrefix(type: PasswordResetRateLimitType): String =
        when (type) {
            PasswordResetRateLimitType.SEND_EMAIL -> SEND_EMAIL_BUCKET_PREFIX
            PasswordResetRateLimitType.CHECK_CODE -> CHECK_CODE_BUCKET_PREFIX
            PasswordResetRateLimitType.MODIFY_PASSWORD -> MODIFY_PASSWORD_BUCKET_PREFIX
        }

    private fun createDisabledResult(capacity: Long) =
        RateLimitConsumeResult(
            consumed = true,
            remainingTokens = capacity,
            secondsToWaitForRefill = 0,
        )

    private fun nanosToSeconds(nanos: Long): Long = nanos / 1_000_000_000

    private fun createBucketConfiguration(config: PasswordResetRateLimitEnvironment.RateLimitConfig): BucketConfiguration {
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

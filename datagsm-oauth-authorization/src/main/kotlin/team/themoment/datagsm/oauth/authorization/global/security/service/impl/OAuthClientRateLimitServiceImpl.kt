package team.themoment.datagsm.oauth.authorization.global.security.service.impl

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.global.data.OAuthClientRateLimitEnvironment
import team.themoment.datagsm.common.global.dto.internal.RateLimitConsumeResult
import team.themoment.datagsm.oauth.authorization.global.security.service.OAuthClientRateLimitService
import java.time.Duration

@Service
class OAuthClientRateLimitServiceImpl(
    private val proxyManager: ProxyManager<String>,
    private val oauthClientRateLimitEnvironment: OAuthClientRateLimitEnvironment,
) : OAuthClientRateLimitService {
    override fun tryConsumeAndReturnRemaining(clientId: String): RateLimitConsumeResult {
        if (!oauthClientRateLimitEnvironment.enabled) {
            return RateLimitConsumeResult(
                consumed = true,
                remainingTokens = oauthClientRateLimitEnvironment.capacity,
                secondsToWaitForRefill = 0,
            )
        }

        val bucket =
            proxyManager.builder().build(
                "rate_limit:oauth_client:$clientId",
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
                .capacity(oauthClientRateLimitEnvironment.capacity)
                .refillIntervally(
                    oauthClientRateLimitEnvironment.refillTokens,
                    Duration.ofSeconds(oauthClientRateLimitEnvironment.refillDurationSeconds),
                ).build()
        return BucketConfiguration
            .builder()
            .addLimit(bandwidth)
            .build()
    }
}

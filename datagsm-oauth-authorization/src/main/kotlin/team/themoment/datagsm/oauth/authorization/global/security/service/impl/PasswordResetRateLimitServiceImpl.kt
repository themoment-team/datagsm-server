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
    override fun tryConsume(
        email: String,
        type: PasswordResetRateLimitType,
    ): RateLimitConsumeResult {
        val config = getConfig(type)

        if (!passwordResetRateLimitEnvironment.enabled) {
            return createDisabledResult(config.capacity)
        }

        val probe = getBucket(email, type, config).tryConsumeAndReturnRemaining(1)
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
            PasswordResetRateLimitType.SIGNUP_SEND_EMAIL -> passwordResetRateLimitEnvironment.signupSend
            PasswordResetRateLimitType.SIGNUP_CHECK_CODE -> passwordResetRateLimitEnvironment.signupCheck
            PasswordResetRateLimitType.LOGIN -> passwordResetRateLimitEnvironment.login
        }

    // 실패한 시도만 세야 하는 한도에서, 성공으로 판명된 시도의 차감을 되돌린다.
    // 먼저 차감하고 나중에 되돌려야 동시에 몰려온 시도도 한도를 넘지 못한다.
    override fun refund(
        email: String,
        type: PasswordResetRateLimitType,
    ) {
        if (!passwordResetRateLimitEnvironment.enabled) return
        getBucket(email, type, getConfig(type)).addTokens(1)
    }

    // 계정 조회는 이메일 대소문자를 구분하지 않으므로, 버킷 키도 정규화해야 대소문자를 바꿔 한도를 우회할 수 없다.
    private fun getBucket(
        email: String,
        type: PasswordResetRateLimitType,
        config: PasswordResetRateLimitEnvironment.RateLimitConfig,
    ) = proxyManager.builder().build(
        "${type.bucketPrefix}${email.trim().lowercase()}",
    ) { createBucketConfiguration(config) }

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

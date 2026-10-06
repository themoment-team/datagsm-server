package team.themoment.datagsm.web.global.security.service.impl

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.global.data.IconUploadRateLimitEnvironment
import team.themoment.datagsm.web.global.security.service.IconUploadRateLimitService
import team.themoment.sdk.exception.ExpectedException
import java.time.Duration

@Service
class IconUploadRateLimitServiceImpl(
    private val proxyManager: ProxyManager<String>,
    private val iconUploadRateLimitEnvironment: IconUploadRateLimitEnvironment,
) : IconUploadRateLimitService {
    override fun ensureNotExceeded(accountEmail: String) {
        if (!iconUploadRateLimitEnvironment.enabled) return

        val bucket =
            proxyManager.builder().build(
                "rate_limit:icon_upload:$accountEmail",
            ) { createBucketConfiguration() }

        if (!bucket.tryConsume(1)) {
            throw ExpectedException("아이콘 업로드 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS)
        }
    }

    private fun createBucketConfiguration(): BucketConfiguration {
        val bandwidth =
            Bandwidth
                .builder()
                .capacity(iconUploadRateLimitEnvironment.capacity)
                .refillIntervally(
                    iconUploadRateLimitEnvironment.refillTokens,
                    Duration.ofSeconds(iconUploadRateLimitEnvironment.refillDurationSeconds),
                ).build()
        return BucketConfiguration
            .builder()
            .addLimit(bandwidth)
            .build()
    }
}

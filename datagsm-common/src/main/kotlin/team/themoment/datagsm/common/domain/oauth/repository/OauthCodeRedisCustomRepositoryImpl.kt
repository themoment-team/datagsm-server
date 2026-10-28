package team.themoment.datagsm.common.domain.oauth.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class OauthCodeRedisCustomRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
) : OauthCodeRedisCustomRepository {
    companion object {
        private const val USED_KEY_PREFIX = "oauth_code_used:"
    }

    override fun claimUsage(
        code: String,
        clientId: String,
        ttl: Duration,
    ): Boolean = redisTemplate.opsForValue().setIfAbsent("$USED_KEY_PREFIX$code", clientId, ttl) == true

    override fun releaseUsage(code: String) {
        redisTemplate.delete("$USED_KEY_PREFIX$code")
    }
}

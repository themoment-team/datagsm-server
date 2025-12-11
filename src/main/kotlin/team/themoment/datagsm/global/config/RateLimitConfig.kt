package team.themoment.datagsm.global.config

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
class RateLimitConfig {
    @Bean(destroyMethod = "shutdown")
    fun redisClient(lettuceConnectionFactory: LettuceConnectionFactory): RedisClient {
        val redisUri = "redis://${lettuceConnectionFactory.hostName}:${lettuceConnectionFactory.port}"
        return RedisClient.create(redisUri)
    }

    @Bean(destroyMethod = "close")
    fun rateLimitConnection(redisClient: RedisClient): StatefulRedisConnection<String, ByteArray> =
        redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))

    @Bean
    fun proxyManager(connection: StatefulRedisConnection<String, ByteArray>): ProxyManager<String> =
        LettuceBasedProxyManager
            .builderFor(connection)
            .withExpirationStrategy(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(60)),
            ).build()
}

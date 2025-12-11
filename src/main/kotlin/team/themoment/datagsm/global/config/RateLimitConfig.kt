package team.themoment.datagsm.global.config

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

@Configuration
class RateLimitConfig(
    private val lettuceConnectionFactory: LettuceConnectionFactory,
) {
    @Bean
    fun proxyManager(): ProxyManager<String> {
        val redisUri = "redis://${lettuceConnectionFactory.hostName}:${lettuceConnectionFactory.port}"
        val redisClient = RedisClient.create(redisUri)

        val connection: StatefulRedisConnection<String, ByteArray> =
            redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))

        return LettuceBasedProxyManager
            .builderFor(connection)
            .build()
    }
}

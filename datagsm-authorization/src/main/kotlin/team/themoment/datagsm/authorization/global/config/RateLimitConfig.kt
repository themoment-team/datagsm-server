package team.themoment.datagsm.authorization.global.config

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
class RateLimitConfig {
    @Bean(destroyMethod = "shutdown")
    fun redisClient(lettuceConnectionFactory: LettuceConnectionFactory): RedisClient {
        val standaloneConfig = lettuceConnectionFactory.standaloneConfiguration as RedisStandaloneConfiguration
        val redisUriBuilder =
            RedisURI
                .builder()
                .withHost(standaloneConfig.hostName)
                .withPort(standaloneConfig.port)
                .withDatabase(standaloneConfig.database)

        standaloneConfig.password.toOptional().ifPresent { passwordChars ->
            redisUriBuilder.withPassword(passwordChars)
        }
        val redisUri = redisUriBuilder.build()
        return RedisClient.create(redisUri)
    }

    @Bean(destroyMethod = "close")
    fun rateLimitConnection(redisClient: RedisClient): StatefulRedisConnection<String, ByteArray> =
        redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))

    @Bean
    fun proxyManager(connection: StatefulRedisConnection<String, ByteArray>): ProxyManager<String> =
        Bucket4jLettuce
            .casBasedBuilder(connection)
            .expirationAfterWrite(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofDays(1)),
            ).build()
}

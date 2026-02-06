package team.themoment.datagsm.common.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

@Configuration
@EnableRedisRepositories(basePackages = ["team.themoment.datagsm.common.domain"])
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private var port: Int = 6379

    @Value("\${spring.data.redis.database:0}")
    private var database: Int = 0

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration()
        config.hostName = host
        config.port = port
        config.database = database
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val jsonMapper =
            JsonMapper
                .builder()
                .addModule(kotlinModule())
                .build()

        val jsonSerializer =
            object : RedisSerializer<Any> {
                override fun serialize(value: Any?): ByteArray = jsonMapper.writeValueAsBytes(value)

                override fun deserialize(bytes: ByteArray?): Any? = bytes?.let { jsonMapper.readValue(it, Any::class.java) }
            }

        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory()
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashValueSerializer = jsonSerializer
            afterPropertiesSet()
        }
    }

    @Bean
    fun indexRedisTemplate(): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory()
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }
}

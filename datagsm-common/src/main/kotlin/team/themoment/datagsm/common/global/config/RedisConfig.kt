package team.themoment.datagsm.common.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    fun objectMapper(): ObjectMapper {
        val typeValidator: PolymorphicTypeValidator =
            BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Any::class.java)
                .build()

        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL)
        }
    }

    @Bean
    fun redisTemplate(objectMapper: ObjectMapper): RedisTemplate<String, Any> {
        val jsonSerializer =
            object : RedisSerializer<Any> {
                override fun serialize(value: Any?): ByteArray = objectMapper.writeValueAsBytes(value)

                override fun deserialize(bytes: ByteArray?): Any? = bytes?.let { objectMapper.readValue(it, Any::class.java) }
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
}

package team.themoment.datagsm.common.domain.oauth.repository

import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.core.RedisKeyValueTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.mapping.RedisMappingContext
import org.springframework.data.redis.repository.support.RedisRepositoryFactory
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import kotlin.reflect.KClass

class DockerAvailableCondition : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = DockerClientFactory.instance().isDockerAvailable
}

@EnabledIf(DockerAvailableCondition::class)
class OauthRefreshTokenRedisRepositoryTest :
    DescribeSpec({

        val redis = GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine")).apply { withExposedPorts(6379) }
        lateinit var connectionFactory: LettuceConnectionFactory
        lateinit var repository: OauthRefreshTokenRedisRepository

        fun buildToken(clientId: String): OauthRefreshTokenRedisEntity =
            OauthRefreshTokenRedisEntity.of(
                email = "test@gsm.hs.kr",
                clientId = clientId,
                token = "refresh-token-$clientId",
                scopes = setOf("app:read"),
                ttl = 60L,
            )

        beforeSpec {
            redis.start()
            connectionFactory =
                LettuceConnectionFactory(redis.host, redis.getMappedPort(6379)).apply {
                    afterPropertiesSet()
                    start()
                }
            val redisTemplate =
                RedisTemplate<ByteArray, ByteArray>().apply {
                    setConnectionFactory(connectionFactory)
                    afterPropertiesSet()
                }
            val mappingContext = RedisMappingContext()
            val keyValueTemplate = RedisKeyValueTemplate(RedisKeyValueAdapter(redisTemplate, mappingContext), mappingContext)
            repository = RedisRepositoryFactory(keyValueTemplate).getRepository(OauthRefreshTokenRedisRepository::class.java)
        }

        afterSpec {
            connectionFactory.destroy()
            redis.stop()
        }

        beforeEach {
            connectionFactory.connection.use { it.serverCommands().flushAll() }
        }

        describe("OauthRefreshTokenRedisRepository 클래스의") {
            describe("deleteById 메서드는") {

                context("idOf로 만든 id로 refresh token을 삭제할 때") {
                    it("Redis에서 토큰과 email, clientId 인덱스가 모두 제거되어야 한다") {
                        val token = buildToken("client-a")
                        repository.save(token)
                        repository.findByEmailAndClientId("test@gsm.hs.kr", "client-a").isPresent shouldBe true

                        repository.deleteById(OauthRefreshTokenRedisEntity.idOf("test@gsm.hs.kr", "client-a"))

                        repository.existsById(token.id) shouldBe false
                        repository.findByEmailAndClientId("test@gsm.hs.kr", "client-a").isPresent shouldBe false
                        repository.findAllByEmail("test@gsm.hs.kr").shouldBeEmpty()
                    }
                }

                context("같은 사용자의 다른 클라이언트 토큰이 있을 때") {
                    it("삭제 대상 클라이언트의 토큰만 제거되어야 한다") {
                        repository.save(buildToken("client-a"))
                        repository.save(buildToken("client-b"))

                        repository.deleteById(OauthRefreshTokenRedisEntity.idOf("test@gsm.hs.kr", "client-a"))

                        repository.findAllByEmail("test@gsm.hs.kr").map { it.clientId } shouldContainExactly listOf("client-b")
                    }
                }
            }
        }
    })

package team.themoment.datagsm.common.global.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer

@Configuration
@Profile("stage")
class EmbeddedRedisConfig :
    InitializingBean,
    DisposableBean {
    private val log = LoggerFactory.getLogger(EmbeddedRedisConfig::class.java)
    private var redisServer: RedisServer? = null

    override fun afterPropertiesSet() {
        log.info("Starting embedded Redis server on port 6379")
        redisServer = RedisServer(6379)
        redisServer!!.start()
    }

    override fun destroy() {
        redisServer?.stop()
    }
}

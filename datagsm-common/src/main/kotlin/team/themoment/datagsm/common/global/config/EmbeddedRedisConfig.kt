package team.themoment.datagsm.common.global.config

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer
import team.themoment.sdk.logging.logger.logger

@Configuration
@Profile("stage")
class EmbeddedRedisConfig :
    InitializingBean,
    DisposableBean {
    private var redisServer: RedisServer? = null

    override fun afterPropertiesSet() {
        logger().info("Starting embedded Redis server on port 6379")
        val server = RedisServer(6379)
        server.start()
        redisServer = server
    }

    override fun destroy() {
        redisServer?.stop()
    }
}

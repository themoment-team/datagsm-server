package team.themoment.datagsm.oauth.authorization.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600)
class RedisSessionConfig

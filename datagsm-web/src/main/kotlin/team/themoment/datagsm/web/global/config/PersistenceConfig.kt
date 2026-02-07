package team.themoment.datagsm.web.global.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableJpaRepositories(
    basePackages = [
        "team.themoment.datagsm.web.domain",
        "team.themoment.datagsm.common.domain",
    ],
)
@EnableRedisRepositories(basePackages = ["team.themoment.datagsm.common.domain"])
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
class PersistenceConfig

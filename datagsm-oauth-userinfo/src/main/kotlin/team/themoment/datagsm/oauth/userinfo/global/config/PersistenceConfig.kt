package team.themoment.datagsm.oauth.userinfo.global.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackages = [
        "team.themoment.datagsm.common.domain",
    ],
)
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
class PersistenceConfig

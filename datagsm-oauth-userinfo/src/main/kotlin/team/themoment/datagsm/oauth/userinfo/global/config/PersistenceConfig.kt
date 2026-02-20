package team.themoment.datagsm.oauth.userinfo.global.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository

@Configuration
@EnableJpaRepositories(
    basePackages = [
        "team.themoment.datagsm.common.domain",
    ],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [JpaRepository::class],
        ),
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [ApiKeyJpaRepository::class],
        ),
    ],
)
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
class PersistenceConfig

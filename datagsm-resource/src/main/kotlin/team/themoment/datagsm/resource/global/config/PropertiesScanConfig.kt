package team.themoment.datagsm.resource.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.CorsEnvironment
import team.themoment.datagsm.common.global.data.NeisEnvironment

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.resource.global.security.data",
        "team.themoment.datagsm.resource.domain.neis.common.data",
    ],
)
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    CorsEnvironment::class,
    NeisEnvironment::class,
)
class PropertiesScanConfig

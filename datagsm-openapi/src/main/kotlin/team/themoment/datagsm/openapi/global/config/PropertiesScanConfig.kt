package team.themoment.datagsm.openapi.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.NeisEnvironment

@Configuration
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    NeisEnvironment::class,
)
class PropertiesScanConfig

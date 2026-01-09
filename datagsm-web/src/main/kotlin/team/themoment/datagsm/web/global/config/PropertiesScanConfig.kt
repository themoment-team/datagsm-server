package team.themoment.datagsm.web.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.CorsEnvironment
import team.themoment.datagsm.common.global.data.JwtEnvironment

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.web.global.security.data",
        "team.themoment.datagsm.web.global.security.jwt",
    ],
)
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    CorsEnvironment::class,
    JwtEnvironment::class,
)
class PropertiesScanConfig

package team.themoment.datagsm.authorization.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.CorsEnvironment
import team.themoment.datagsm.common.global.data.EmailRateLimitEnvironment
import team.themoment.datagsm.common.global.data.JwtProperties
import team.themoment.datagsm.common.global.data.OauthProperties

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.authorization.global.security.data",
    ],
)
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    CorsEnvironment::class,
    EmailRateLimitEnvironment::class,
    JwtProperties::class,
    OauthProperties::class,
)
class PropertiesScanConfig
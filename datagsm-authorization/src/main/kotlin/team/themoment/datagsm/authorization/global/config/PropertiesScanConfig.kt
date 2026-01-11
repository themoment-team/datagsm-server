package team.themoment.datagsm.authorization.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
import team.themoment.datagsm.common.global.data.CorsEnvironment
import team.themoment.datagsm.common.global.data.EmailRateLimitEnvironment
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment

@Configuration
@EnableConfigurationProperties(
    ApiKeyEnvironment::class,
    CorsEnvironment::class,
    EmailRateLimitEnvironment::class,
    OauthJwtEnvironment::class,
    OauthEnvironment::class,
)
class PropertiesScanConfig

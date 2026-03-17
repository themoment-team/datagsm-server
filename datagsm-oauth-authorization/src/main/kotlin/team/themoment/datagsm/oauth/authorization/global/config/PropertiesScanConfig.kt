package team.themoment.datagsm.oauth.authorization.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.data.CorsEnvironment
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.data.PasswordResetRateLimitEnvironment
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment

@Configuration
@EnableConfigurationProperties(
    CorsEnvironment::class,
    OauthJwtProvisionEnvironment::class,
    OauthEnvironment::class,
    PasswordResetRateLimitEnvironment::class,
)
class PropertiesScanConfig

package team.themoment.datagsm.oauth.userinfo.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.oauth.userinfo.global.data.OauthJwtVerificationEnvironment

@Configuration
@EnableConfigurationProperties(
    OauthJwtVerificationEnvironment::class,
)
class PropertiesScanConfig

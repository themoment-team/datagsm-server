package team.themoment.datagsm.authorization.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.authorization.global.security.jwt",
        "team.themoment.datagsm.authorization.domain.oauth.property",
        "team.themoment.datagsm.authorization.global.thirdparty.feign.resource",
    ],
)
class PropertiesScanConfig

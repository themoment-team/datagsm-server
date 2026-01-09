package team.themoment.datagsm.authorization.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.authorization.global.security.data",
        "team.themoment.datagsm.common.global.data",
    ],
)
class PropertiesScanConfig

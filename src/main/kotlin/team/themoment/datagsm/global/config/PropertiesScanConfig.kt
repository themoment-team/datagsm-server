package team.themoment.datagsm.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.global.security.data",
        "team.themoment.datagsm.global.security.jwt",
        "team.themoment.datagsm.global.config.neis",
    ],
)
class PropertiesScanConfig

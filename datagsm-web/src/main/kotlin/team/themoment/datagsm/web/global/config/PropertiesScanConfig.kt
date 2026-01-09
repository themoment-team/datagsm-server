package team.themoment.datagsm.web.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.web.global.security.data",
        "team.themoment.datagsm.web.global.security.jwt",
        "team.themoment.datagsm.common.global.data",
    ],
)
class PropertiesScanConfig

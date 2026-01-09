package team.themoment.datagsm.resource.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "team.themoment.datagsm.resource.global.security.data",
        "team.themoment.datagsm.resource.domain.neis.common.data",
    ],
)
class PropertiesScanConfig

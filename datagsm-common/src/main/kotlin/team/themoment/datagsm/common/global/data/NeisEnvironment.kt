package team.themoment.datagsm.common.global.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cloud.neis.api")
data class NeisEnvironment(
    val key: String,
    val officeCode: String,
    val schoolCode: String,
)

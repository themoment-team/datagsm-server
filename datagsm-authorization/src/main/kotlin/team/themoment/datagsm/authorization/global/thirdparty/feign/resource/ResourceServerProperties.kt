package team.themoment.datagsm.authorization.global.thirdparty.feign.resource

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.resource-server")
data class ResourceServerProperties(
    val apiKey: String,
    val url: String,
)

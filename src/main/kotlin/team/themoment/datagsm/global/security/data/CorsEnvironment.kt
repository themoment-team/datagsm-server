package team.themoment.datagsm.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.cors")
data class CorsEnvironment(
    val allowedOrigins: List<String> = listOf("*"),
)

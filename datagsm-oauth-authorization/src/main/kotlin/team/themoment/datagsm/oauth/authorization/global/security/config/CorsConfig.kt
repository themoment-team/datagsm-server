package team.themoment.datagsm.oauth.authorization.global.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import team.themoment.datagsm.common.global.data.CorsEnvironment

@Configuration
class CorsConfig(
    private val corsEnvironment: CorsEnvironment,
) {
    @Bean
    fun configure(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = corsEnvironment.allowedOrigins
                allowedMethods = listOf("*")
                allowedHeaders = listOf("*")
                allowCredentials = true
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}

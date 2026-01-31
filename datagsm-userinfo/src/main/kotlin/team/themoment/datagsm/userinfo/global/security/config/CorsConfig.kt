package team.themoment.datagsm.userinfo.global.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
                allowedMethods = HttpMethod.values().map(HttpMethod::name)
                addAllowedHeader("*")
                allowCredentials = true
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}

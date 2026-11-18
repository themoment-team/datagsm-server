package team.themoment.datagsm.openapi.global.config

import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.AntPathMatcher
import team.themoment.datagsm.common.global.config.SecurityRequirementCustomizer

@Configuration
class SwaggerSecurityConfig {
    private val pathMatcher = AntPathMatcher()

    @Bean
    fun securityRequirementCustomizer(): GlobalOpenApiCustomizer =
        SecurityRequirementCustomizer(
            schemeName = "apiKeyAuth",
            securityScheme =
                SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name("X-API-KEY")
                    .description("DataGSM 웹에서 발급받은 API Key(UUID)를 X-API-KEY 헤더로 전달합니다."),
            isPublicPath = { path -> API_KEY_EXEMPT_PATHS.any { pathMatcher.match(it, path) } },
        )

    companion object {
        // ApiKeyAuthenticationFilter가 API Key 검사를 건너뛰는 경로와 같아야 한다.
        private val API_KEY_EXEMPT_PATHS = listOf("/v1/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
    }
}

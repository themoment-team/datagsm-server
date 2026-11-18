package team.themoment.datagsm.web.global.config

import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.AntPathMatcher
import team.themoment.datagsm.common.global.config.SecurityRequirementCustomizer
import team.themoment.datagsm.web.global.security.config.AuthenticationPathConfig

@Configuration
class SwaggerSecurityConfig {
    private val pathMatcher = AntPathMatcher()

    @Bean
    fun securityRequirementCustomizer(): GlobalOpenApiCustomizer =
        SecurityRequirementCustomizer(
            schemeName = "bearerAuth",
            securityScheme =
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("DataGSM 인가 서버가 웹 클라이언트에 발급한 액세스 토큰을 Authorization 헤더에 Bearer 형식으로 전달합니다."),
            isPublicPath = { path -> AuthenticationPathConfig.PUBLIC_PATHS.any { pathMatcher.match(it, path) } },
        )
}

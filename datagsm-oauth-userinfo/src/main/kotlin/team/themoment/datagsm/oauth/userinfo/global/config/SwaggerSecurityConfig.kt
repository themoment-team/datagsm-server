package team.themoment.datagsm.oauth.userinfo.global.config

import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.common.global.config.SecurityRequirementCustomizer

@Configuration
class SwaggerSecurityConfig {
    @Bean
    fun securityRequirementCustomizer(): GlobalOpenApiCustomizer =
        SecurityRequirementCustomizer(
            schemeName = "bearerAuth",
            securityScheme =
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("DataGSM 인가 서버가 발급한 OAuth 2.0 액세스 토큰을 Authorization 헤더에 Bearer 형식으로 전달합니다."),
            // SecurityConfig가 /userinfo에만 권한을 요구하고 나머지는 permitAll이다.
            isPublicPath = { path -> path != USERINFO_PATH },
        )

    companion object {
        private const val USERINFO_PATH = "/userinfo"
    }
}

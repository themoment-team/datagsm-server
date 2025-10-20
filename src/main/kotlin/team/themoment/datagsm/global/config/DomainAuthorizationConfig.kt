package team.themoment.datagsm.global.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.auth.entity.constant.Role

@Component
class DomainAuthorizationConfig {
    fun configure(
        authorizeRequests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        authorizeRequests
            // Swagger
            .requestMatchers("/swagger-ui/**", "/api-docs/**")
            .permitAll()
            // Health Check
            .requestMatchers("/v1/health")
            .permitAll()
            // Auth
            .requestMatchers("/v1/auth/google")
            .permitAll()
            .requestMatchers("/v1/auth/api-key")
            .hasAnyAuthority(
                Role.GENERAL_STUDENT.authority,
                Role.STUDENT_COUNCIL.authority,
                Role.ADMIN.authority,
                Role.LIBRARY_MANAGER.authority,
                Role.DORMITORY_MANAGER.authority,
                Role.MEDIA_DEPARTMENT.authority,
            )
            // Others
            .anyRequest()
            .permitAll()
    }
}

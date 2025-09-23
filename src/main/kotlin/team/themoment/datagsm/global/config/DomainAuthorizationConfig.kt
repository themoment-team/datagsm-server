package team.themoment.datagsm.global.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component

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
            // Others
            .anyRequest()
            .permitAll()
    }
}

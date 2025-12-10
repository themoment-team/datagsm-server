package team.themoment.datagsm.global.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component

@Component
class DomainAuthorizationConfig {
    fun configure(authorizeRequests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        authorizeRequests
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v1/health")
            .permitAll()
            .requestMatchers("/v1/auth/google", "/v1/auth/refresh")
            .permitAll()
            .requestMatchers("/v1/auth/api-key/**")
            .authenticated()
            .anyRequest()
            .authenticated()
    }
}

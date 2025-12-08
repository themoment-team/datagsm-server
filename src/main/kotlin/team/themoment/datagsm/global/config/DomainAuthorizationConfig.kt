package team.themoment.datagsm.global.config

import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.account.entity.constant.AccountRole

@Component
class DomainAuthorizationConfig {
    fun configure(authorizeRequests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        authorizeRequests
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v1/health")
            .permitAll()
            .requestMatchers("/v1/auth/google", "/v1/auth/refresh")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/students", "/v1/clubs", "/v1/projects")
            .hasAnyAuthority(
                AccountRole.API_KEY_USER.authority,
                AccountRole.ADMIN.authority,
                AccountRole.ROOT.authority,
            ).requestMatchers("/v1/students/**", "/v1/clubs/**", "/v1/projects/**")
            .hasAnyRole(AccountRole.ADMIN.name, AccountRole.ROOT.name)
            .requestMatchers("/v1/auth/api-key/**")
            .authenticated()
            .anyRequest()
            .authenticated()
    }
}

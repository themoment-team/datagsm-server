package team.themoment.datagsm.global.config

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
            .requestMatchers("/v1/auth/api-key/**")
            .hasAnyAuthority(AccountRole.USER.authority, AccountRole.ADMIN.authority, AccountRole.ROOT.authority)
            .requestMatchers("/v1/club/**", "/v1/project/**", "/v1/students/**")
            .hasAnyAuthority(AccountRole.ADMIN.authority, AccountRole.ROOT.authority, AccountRole.API_KEY_USER.authority)
            .anyRequest()
            .authenticated()
    }
}

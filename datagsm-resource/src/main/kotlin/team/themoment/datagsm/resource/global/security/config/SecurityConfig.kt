package team.themoment.datagsm.resource.global.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.resource.global.security.authentication.principal.PrincipalProvider
import team.themoment.datagsm.resource.global.security.filter.ApiKeyAuthenticationFilter
import team.themoment.datagsm.resource.global.security.filter.RateLimitFilter
import team.themoment.datagsm.resource.global.security.handler.CustomAuthenticationEntryPoint
import team.themoment.datagsm.resource.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.resource.global.security.service.RateLimitService

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    @param:Qualifier("configure") private val corsConfigurationSource: CorsConfigurationSource,
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val principalProvider: PrincipalProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val rateLimitService: RateLimitService,
    private val objectMapper: ObjectMapper,
    private val currentUserProvider: CurrentUserProvider,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf(CsrfConfigurer<*>::disable)
            .cors { it.configurationSource(corsConfigurationSource) }
            .httpBasic(HttpBasicConfigurer<*>::disable)
            .formLogin(FormLoginConfigurer<*>::disable)
            .logout(LogoutConfigurer<*>::disable)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(customAuthenticationEntryPoint) }
            .addFilterBefore(
                ApiKeyAuthenticationFilter(apiKeyJpaRepository, principalProvider),
                UsernamePasswordAuthenticationFilter::class.java,
            ).addFilterBefore(
                RateLimitFilter(rateLimitService, objectMapper, currentUserProvider),
                UsernamePasswordAuthenticationFilter::class.java,
            ).authorizeHttpRequests {
                it
                    .requestMatchers(*AuthenticationPathConfig.PUBLIC_PATHS.toTypedArray())
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }

        return http.build()
    }

    @Bean
    fun annotationTemplateExpressionDefaults() = AnnotationTemplateExpressionDefaults()
}

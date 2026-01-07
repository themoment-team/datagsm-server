package team.themoment.datagsm.web.global.config

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
import team.themoment.datagsm.global.security.config.AuthenticationPathConfig
import team.themoment.datagsm.web.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.web.global.security.authentication.principal.PrincipalProvider
import team.themoment.datagsm.web.global.security.handler.CustomAuthenticationEntryPoint
import team.themoment.datagsm.web.global.security.jwt.JwtProvider
import team.themoment.datagsm.web.global.security.jwt.filter.JwtAuthenticationFilter
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    @param:Qualifier("configure") private val corsConfigurationSource: CorsConfigurationSource,
    private val jwtProvider: JwtProvider,
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val principalProvider: PrincipalProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
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
                JwtAuthenticationFilter(jwtProvider, principalProvider),
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

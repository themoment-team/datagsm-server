package team.themoment.datagsm.global.security.config

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
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.global.config.DomainAuthorizationConfig
import team.themoment.datagsm.global.security.filter.ApiKeyAuthenticationFilter
import team.themoment.datagsm.global.security.handler.CustomAuthenticationEntryPoint
import team.themoment.datagsm.global.security.jwt.JwtProvider
import team.themoment.datagsm.global.security.jwt.filter.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val domainAuthorizationConfig: DomainAuthorizationConfig,
    @param:Qualifier("configure") private val corsConfigurationSource: CorsConfigurationSource,
    private val jwtProvider: JwtProvider,
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
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
                ApiKeyAuthenticationFilter(apiKeyJpaRepository),
                UsernamePasswordAuthenticationFilter::class.java,
            ).addFilterBefore(JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { domainAuthorizationConfig.configure(it) }

        return http.build()
    }

    @Bean
    fun annotationTemplateExpressionDefaults() = AnnotationTemplateExpressionDefaults()
}

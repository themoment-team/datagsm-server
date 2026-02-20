package team.themoment.datagsm.web.global.security.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.web.global.security.handler.CustomAuthenticationEntryPoint
import team.themoment.datagsm.web.global.security.jwt.JwtProvider
import team.themoment.datagsm.web.global.security.jwt.filter.JwtAuthenticationFilter
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    @param:Qualifier("configure") private val corsConfigurationSource: CorsConfigurationSource,
    private val jwtProvider: JwtProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val objectMapper: ObjectMapper,
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
                JwtAuthenticationFilter(jwtProvider, objectMapper),
                UsernamePasswordAuthenticationFilter::class.java,
            ).authorizeHttpRequests {
                it
                    .requestMatchers(*AuthenticationPathConfig.PUBLIC_PATHS.toTypedArray())
                    .permitAll()
                    .requestMatchers(
                        "/v1/students/**",
                        "/v1/projects/**",
                        "/v1/clubs/**",
                    ).hasAnyRole(AccountRole.ADMIN.name, AccountRole.ROOT.name)
                    .requestMatchers(HttpMethod.GET, "/v1/auth/api-keys")
                    .hasAnyRole(AccountRole.ADMIN.name, AccountRole.ROOT.name)
                    .requestMatchers(HttpMethod.DELETE, "/v1/auth/api-keys/{apiKeyId}")
                    .hasAnyRole(AccountRole.ADMIN.name, AccountRole.ROOT.name)
                    .requestMatchers(HttpMethod.GET, "/v1/clients")
                    .hasAnyRole(AccountRole.ADMIN.name, AccountRole.ROOT.name)
                    .anyRequest()
                    .authenticated()
            }

        return http.build()
    }

    @Bean
    fun annotationTemplateExpressionDefaults() = AnnotationTemplateExpressionDefaults()
}

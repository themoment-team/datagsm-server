package team.themoment.datagsm.oauth.userinfo.global.security.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource
import team.themoment.datagsm.common.global.data.OAuthClientRateLimitEnvironment
import team.themoment.datagsm.oauth.userinfo.global.data.OauthJwtVerificationEnvironment
import team.themoment.datagsm.oauth.userinfo.global.security.filter.OAuthClientRateLimitFilter
import team.themoment.datagsm.oauth.userinfo.global.security.handler.CustomAuthenticationEntryPoint
import team.themoment.datagsm.oauth.userinfo.global.security.jwt.JwtProvider
import team.themoment.datagsm.oauth.userinfo.global.security.jwt.filter.JwtAuthenticationFilter
import team.themoment.datagsm.oauth.userinfo.global.security.service.OAuthClientRateLimitService
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @param:Qualifier("configure") private val corsConfigurationSource: CorsConfigurationSource,
    private val jwtProvider: JwtProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val objectMapper: ObjectMapper,
    private val oauthClientRateLimitService: OAuthClientRateLimitService,
    private val oauthClientRateLimitEnvironment: OAuthClientRateLimitEnvironment,
    private val oauthJwtVerificationEnvironment: OauthJwtVerificationEnvironment,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val selfReadAuthority = "SCOPE_${oauthJwtVerificationEnvironment.datagsmApplicationId}:self_read"

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
            ).addFilterAfter(
                OAuthClientRateLimitFilter(oauthClientRateLimitService, oauthClientRateLimitEnvironment, objectMapper),
                JwtAuthenticationFilter::class.java,
            ).authorizeHttpRequests {
                it
                    .requestMatchers("/userinfo")
                    .hasAuthority(selfReadAuthority)
                    .anyRequest()
                    .permitAll()
            }

        return http.build()
    }
}

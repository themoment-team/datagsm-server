package team.themoment.datagsm.resource.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.resource.global.security.authentication.CustomAuthenticationToken
import team.themoment.datagsm.resource.global.security.authentication.principal.PrincipalProvider
import team.themoment.datagsm.resource.global.security.config.AuthenticationPathConfig
import java.util.UUID

class ApiKeyAuthenticationFilter(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val principalProvider: PrincipalProvider,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI
        return AuthenticationPathConfig.PUBLIC_PATHS.any { path ->
            pathMatcher.match(path, requestPath)
        }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val apiKeyHeader = request.getHeader("X-API-KEY")
        if (apiKeyHeader.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }
        try {
            val apiKeyValue = UUID.fromString(apiKeyHeader)
            val apiKey =
                apiKeyJpaRepository
                    .findByValue(apiKeyValue)
                    .orElse(null)
            if (apiKey == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 API Key입니다.")
                return
            }
            if (apiKey.isExpired()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "만료된 API Key입니다.")
                return
            }
            val scopeAuthorities = apiKey.scopes.map { SimpleGrantedAuthority("SCOPE_$it") }

            val authentication =
                CustomAuthenticationToken(
                    principal = principalProvider.provideFromApiKey(apiKey),
                    authorities = scopeAuthorities,
                )
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: IllegalArgumentException) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "잘못된 형식의 API Key입니다.")
            return
        }
        filterChain.doFilter(request, response)
    }
}

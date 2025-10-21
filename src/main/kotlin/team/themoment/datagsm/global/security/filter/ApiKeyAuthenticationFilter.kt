package team.themoment.datagsm.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.global.security.config.AuthenticationPathConfig
import java.util.UUID

class ApiKeyAuthenticationFilter(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI
        val isApiKeyPath =
            AuthenticationPathConfig.API_KEY_PATHS.any { path ->
                pathMatcher.match(path, requestPath)
            }
        return !isApiKeyPath
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val apiKeyHeader = request.getHeader("X-API-KEY")

        if (apiKeyHeader.isNullOrBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "API Key가 필요합니다.")
            return
        }

        try {
            val apiKeyValue = UUID.fromString(apiKeyHeader)
            val apiKey =
                apiKeyJpaRepository
                    .findByApiKeyValue(apiKeyValue)
                    .orElse(null)

            if (apiKey == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 API Key입니다.")
                return
            }

            val student = apiKey.apiKeyStudent
            val email = student?.studentEmail ?: ""
            val role = student?.studentRole

            val authentication =
                UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    role?.let { listOf(it) } ?: emptyList(),
                )

            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: IllegalArgumentException) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "잘못된 형식의 API Key입니다.")
            return
        }

        filterChain.doFilter(request, response)
    }
}
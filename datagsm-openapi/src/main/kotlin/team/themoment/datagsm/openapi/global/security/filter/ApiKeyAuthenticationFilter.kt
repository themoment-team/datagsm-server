package team.themoment.datagsm.resource.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.resource.global.security.authentication.ApiKeyAuthenticationToken
import team.themoment.datagsm.resource.global.security.authentication.principal.ApiKeyPrincipal
import java.util.UUID

class ApiKeyAuthenticationFilter(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
) : OncePerRequestFilter() {
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
            val scopeAuthorities = apiKey.scopes.mapNotNull { ApiKeyScope.fromString(it) }.toSet()
            if (scopeAuthorities.size != apiKey.scopes.size) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 scope를 포함한 API Key입니다.")
                return
            }
            val authentication =
                ApiKeyAuthenticationToken(
                    ApiKeyPrincipal(
                        email = apiKey.account.email,
                        apiKey = apiKey,
                    ),
                    scopeAuthorities,
                )
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: IllegalArgumentException) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "잘못된 형식의 API Key입니다.")
            return
        }
        filterChain.doFilter(request, response)
    }
}

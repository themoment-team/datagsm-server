package team.themoment.datagsm.openapi.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.domain.auth.entity.constant.ApiKeyScope
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.global.security.util.SecurityFilterResponseUtil
import team.themoment.datagsm.openapi.global.security.authentication.ApiKeyAuthenticationToken
import team.themoment.datagsm.openapi.global.security.authentication.principal.ApiKeyPrincipal
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class ApiKeyAuthenticationFilter(
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val objectMapper: ObjectMapper,
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
                SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "유효하지 않은 API Key입니다.")
                return
            }
            if (apiKey.isExpired()) {
                SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "만료된 API Key입니다.")
                return
            }
            val scopeAuthorities = apiKey.scopes.mapNotNull { ApiKeyScope.fromString(it) }.toSet()
            if (scopeAuthorities.size != apiKey.scopes.size) {
                SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "유효하지 않은 scope를 포함한 API Key입니다.")
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
        } catch (_: IllegalArgumentException) {
            SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "잘못된 형식의 API Key입니다.")
            return
        }
        filterChain.doFilter(request, response)
    }
}

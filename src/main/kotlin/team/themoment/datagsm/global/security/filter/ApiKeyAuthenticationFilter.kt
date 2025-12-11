package team.themoment.datagsm.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.entity.constant.ApiScope
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
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
            val account = apiKey.account
            val email = account?.email ?: ""
            val role = account?.role

            val scopeAuthorities =
                if (role == AccountRole.ADMIN || role == AccountRole.ROOT) {
                    listOf(SimpleGrantedAuthority("SCOPE_${ApiScope.ALL_SCOPE}"))
                } else {
                    apiKey.scopes.map { SimpleGrantedAuthority("SCOPE_$it") }
                }

            val authorities = listOfNotNull(role, AccountRole.API_KEY_USER) + scopeAuthorities
            val authentication =
                UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    authorities,
                )
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: IllegalArgumentException) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "잘못된 형식의 API Key입니다.")
            return
        }
        filterChain.doFilter(request, response)
    }
}

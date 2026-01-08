package team.themoment.datagsm.resource.global.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.resource.global.security.authentication.CustomAuthenticationToken
import team.themoment.datagsm.resource.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.resource.global.security.service.RateLimitService
import team.themoment.sdk.response.CommonApiResponse

class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    private val objectMapper: ObjectMapper,
    private val currentUserProvider: CurrentUserProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication !is CustomAuthenticationToken) {
            filterChain.doFilter(request, response)
            return
        }

        val apiKey = currentUserProvider.getPrincipal().apiKey
        if (apiKey == null) {
            filterChain.doFilter(request, response)
            return
        }
        if (isExcludedFromRateLimit(apiKey)) {
            filterChain.doFilter(request, response)
            return
        }

        val result = rateLimitService.tryConsumeAndReturnRemaining(apiKey)
        response.setHeader("X-RateLimit-Limit", apiKey.rateLimitCapacity.toString())
        response.setHeader("X-RateLimit-Remaining", result.remainingTokens.toString())

        if (!result.consumed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.setHeader("Retry-After", result.secondsToWaitForRefill.toString())
            val errorResponse =
                CommonApiResponse.error(
                    "API 요청 제한을 초과했습니다. ${result.secondsToWaitForRefill}초 후에 다시 시도해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS,
                )
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun isExcludedFromRateLimit(apiKey: ApiKey): Boolean =
        apiKey.account.role in setOf(AccountRole.ADMIN, AccountRole.ROOT) ||
            apiKey.scopes.any { it.startsWith("admin:") }
}

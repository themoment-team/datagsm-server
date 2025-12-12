package team.themoment.datagsm.global.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.security.service.RateLimitService

class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null ||
            !authentication.authorities.any { it.authority == AccountRole.API_KEY_USER.authority }
        ) {
            filterChain.doFilter(request, response)
            return
        }
        val apiKey = authentication.details as? ApiKey
        if (apiKey == null) {
            filterChain.doFilter(request, response)
            return
        }
        if (apiKey.account.role in setOf(AccountRole.ADMIN, AccountRole.ROOT)) {
            filterChain.doFilter(request, response)
            return
        }
        val hasAdminScope =
            apiKey.scopes.any { scope ->
                scope.startsWith("admin:")
            }
        if (hasAdminScope) {
            filterChain.doFilter(request, response)
            return
        }
        val allowed = rateLimitService.tryConsume(apiKey)
        if (!allowed) {
            val remainingTokens = rateLimitService.getRemainingTokens(apiKey)
            val secondsUntilRefill = rateLimitService.getSecondsUntilRefill(apiKey)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.setHeader("X-RateLimit-Limit", apiKey.rateLimitCapacity.toString())
            response.setHeader("X-RateLimit-Remaining", remainingTokens.toString())
            response.setHeader("Retry-After", secondsUntilRefill.toString())
            val errorResponse =
                CommonApiResponse.error(
                    "API 요청 제한을 초과했습니다. ${secondsUntilRefill}초 후에 다시 시도해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS,
                )
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            return
        }
        response.setHeader("X-RateLimit-Limit", apiKey.rateLimitCapacity.toString())
        response.setHeader("X-RateLimit-Remaining", rateLimitService.getRemainingTokens(apiKey).toString())
        filterChain.doFilter(request, response)
    }
}

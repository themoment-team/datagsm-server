package team.themoment.datagsm.oauth.userinfo.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.global.data.OAuthClientRateLimitEnvironment
import team.themoment.datagsm.oauth.userinfo.global.security.authentication.OauthAuthenticationToken
import team.themoment.datagsm.oauth.userinfo.global.security.service.OAuthClientRateLimitService
import team.themoment.sdk.response.CommonApiResponse
import tools.jackson.databind.ObjectMapper

class OAuthClientRateLimitFilter(
    private val oauthClientRateLimitService: OAuthClientRateLimitService,
    private val oauthClientRateLimitEnvironment: OAuthClientRateLimitEnvironment,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication !is OauthAuthenticationToken) {
            filterChain.doFilter(request, response)
            return
        }

        val clientId = authentication.principal.clientId
        val result = oauthClientRateLimitService.tryConsumeAndReturnRemaining(clientId)

        response.setHeader("X-RateLimit-Limit", oauthClientRateLimitEnvironment.capacity.toString())
        response.setHeader("X-RateLimit-Remaining", result.remainingTokens.toString())

        if (!result.consumed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.setHeader(HttpHeaders.RETRY_AFTER, result.secondsToWaitForRefill.toString())
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
}

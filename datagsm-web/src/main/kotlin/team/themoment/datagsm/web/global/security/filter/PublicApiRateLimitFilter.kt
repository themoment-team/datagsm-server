package team.themoment.datagsm.web.global.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.global.data.PublicApiRateLimitEnvironment
import team.themoment.datagsm.web.global.security.service.PublicApiRateLimitService
import team.themoment.sdk.response.CommonApiResponse
import tools.jackson.databind.ObjectMapper

class PublicApiRateLimitFilter(
    private val publicApiRateLimitService: PublicApiRateLimitService,
    private val publicApiRateLimitEnvironment: PublicApiRateLimitEnvironment,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = !request.requestURI.startsWith(PUBLIC_API_PREFIX)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val clientIp = resolveClientIp(request)
        val result = publicApiRateLimitService.tryConsumeAndReturnRemaining(clientIp)

        response.setHeader("X-RateLimit-Limit", publicApiRateLimitEnvironment.capacity.toString())
        response.setHeader("X-RateLimit-Remaining", result.remainingTokens.toString())

        if (!result.consumed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.characterEncoding = "UTF-8"
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

    /**
     * ALB가 덧붙이는 X-Forwarded-For의 마지막 항목이 ALB가 관측한 실제 접속 IP다.
     * 클라이언트가 임의로 넣은 앞쪽 값은 신뢰할 수 없으므로 사용하지 않는다.
     */
    private fun resolveClientIp(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER)
        if (forwardedFor.isNullOrBlank()) return request.remoteAddr

        return forwardedFor
            .split(",")
            .map { it.trim() }
            .lastOrNull { it.isNotEmpty() }
            ?: request.remoteAddr
    }

    private companion object {
        const val PUBLIC_API_PREFIX = "/v1/public/"
        const val X_FORWARDED_FOR_HEADER = "X-Forwarded-For"
    }
}

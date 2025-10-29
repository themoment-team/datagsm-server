package team.themoment.datagsm.global.common.logging.filter

import com.github.snowykte0426.peanut.butter.logging.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import team.themoment.datagsm.global.common.logging.wrapper.CachedRequestBodyWrapper
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class LoggingFilter : OncePerRequestFilter() {
    companion object {
        private val NOT_LOGGING_URL =
            arrayOf(
                "/v3/api-docs/**",
                "/swagger-ui/**",
            )
    }

    private val matcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (isNotLoggingURL(request.requestURI)) {
            executeFilterWithExceptionHandling { filterChain.doFilter(request, response) }
            return
        }

        when {
            isMultipart(request) -> handleMultipartRequest(request, response, filterChain)
            else -> handleRegularRequest(request, response, filterChain)
        }
    }

    private fun handleMultipartRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val responseWrapper = ContentCachingResponseWrapper(response)
        val logId = UUID.randomUUID()
        val startTime = System.currentTimeMillis()

        try {
            requestLoggingMultipart(request, logId)
            filterChain.doFilter(request, responseWrapper)
        } catch (e: Exception) {
            logger().error("LoggingFilter의 FilterChain에서 예외가 발생했습니다.", e)
        } finally {
            responseLogging(responseWrapper, startTime, logId)
            copyResponseBody(responseWrapper)
        }
    }

    private fun handleRegularRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cachedRequest =
            createCachedRequest(request) ?: run {
                executeFilterWithExceptionHandling { filterChain.doFilter(request, response) }
                return
            }

        val responseWrapper = ContentCachingResponseWrapper(response)
        val logId = UUID.randomUUID()
        val startTime = System.currentTimeMillis()

        try {
            requestLogging(cachedRequest, logId, cachedRequest.cachedBody)
            filterChain.doFilter(cachedRequest, responseWrapper)
        } catch (e: Exception) {
            logger().error("LoggingFilter의 FilterChain에서 예외가 발생했습니다.", e)
        } finally {
            responseLogging(responseWrapper, startTime, logId)
            copyResponseBody(responseWrapper)
        }
    }

    private fun createCachedRequest(request: HttpServletRequest): CachedRequestBodyWrapper? =
        try {
            CachedRequestBodyWrapper(request)
        } catch (e: IOException) {
            logger().error("요청 바디 캐싱 중 예외 발생 - 원본 요청으로 진행합니다.", e)
            null
        }

    private fun executeFilterWithExceptionHandling(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            logger().error("로깅 제외 경로 예외", e)
        }
    }

    private fun copyResponseBody(responseWrapper: ContentCachingResponseWrapper) {
        try {
            responseWrapper.copyBodyToResponse()
        } catch (e: IOException) {
            logger().error("LoggingFilter에서 response body를 출력하는 도중 예외가 발생했습니다.", e)
        }
    }

    private fun isNotLoggingURL(requestURI: String): Boolean = NOT_LOGGING_URL.any { pattern -> matcher.match(pattern, requestURI) }

    private fun isMultipart(request: HttpServletRequest): Boolean = request.contentType?.lowercase()?.startsWith("multipart/") ?: false

    private fun requestLogging(
        request: HttpServletRequest,
        logId: UUID,
        cachedBody: ByteArray,
    ) {
        logger().info(
            "Log-ID: {}, IP: {}, URI: {}, Http-Method: {}, Params: {}, Content-Type: {}, User-Cookies: {}, User-Agent: {}, Request-Body: {}",
            logId,
            request.remoteAddr,
            request.requestURI,
            request.method,
            request.queryString,
            request.contentType,
            formatCookies(request.cookies),
            request.getHeader("User-Agent"),
            getRequestBody(cachedBody),
        )
    }

    private fun requestLoggingMultipart(
        request: HttpServletRequest,
        logId: UUID,
    ) {
        val contentLength = request.getHeader("Content-Length") ?: "[unknown]"

        logger().info(
            "Log-ID: {}, IP: {}, URI: {}, Http-Method: {}, Params: {}, Content-Type: {}, Content-Length: {}, User-Cookies: {}, User-Agent: {}, Request-Body: {}",
            logId,
            request.remoteAddr,
            request.requestURI,
            request.method,
            request.queryString,
            request.contentType,
            contentLength,
            formatCookies(request.cookies),
            request.getHeader("User-Agent"),
            "[multipart omitted]",
        )
    }

    private fun responseLogging(
        response: ContentCachingResponseWrapper,
        startTime: Long,
        logId: UUID,
    ) {
        val responseTime = System.currentTimeMillis() - startTime
        val responseBody = String(response.contentAsByteArray, StandardCharsets.UTF_8)

        logger().info(
            "Log-ID: {}, Status-Code: {}, Content-Type: {}, Response Time: {}ms, Response-Body: {}",
            logId,
            response.status,
            response.contentType,
            responseTime,
            responseBody,
        )
    }

    private fun getRequestBody(byteArrayContent: ByteArray): String {
        val oneLineContent =
            String(byteArrayContent, StandardCharsets.UTF_8)
                .replace("\\s".toRegex(), "")
        return if (StringUtils.hasText(oneLineContent)) oneLineContent else "[empty]"
    }

    private fun formatCookies(cookies: Array<Cookie>?): String = cookies?.joinToString(", ") { "${it.name}=${it.value}" } ?: "[none]"
}

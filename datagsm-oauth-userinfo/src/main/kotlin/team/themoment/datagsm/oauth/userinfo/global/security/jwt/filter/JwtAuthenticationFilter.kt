package team.themoment.datagsm.oauth.userinfo.global.security.jwt.filter

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.common.global.security.util.SecurityFilterResponseUtil
import team.themoment.datagsm.oauth.userinfo.global.security.jwt.JwtProvider
import team.themoment.sdk.logging.logger.logger
import tools.jackson.databind.ObjectMapper

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val existingAuth = SecurityContextHolder.getContext().authentication
        if (existingAuth != null && existingAuth.isAuthenticated) {
            filterChain.doFilter(request, response)
            return
        }

        val bearerToken = request.getHeader("Authorization")
        if (bearerToken.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val token = jwtProvider.extractToken(bearerToken)
        if (token == null) {
            SecurityFilterResponseUtil.sendErrorResponse(
                response,
                objectMapper,
                "잘못된 형식의 Authorization 헤더입니다. Bearer 토큰을 사용해주세요.",
            )
            return
        }

        try {
            val authentication = jwtProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (_: ExpiredJwtException) {
            SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "만료된 토큰입니다.")
            return
        } catch (_: MalformedJwtException) {
            SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "잘못된 형식의 토큰입니다.")
            return
        } catch (_: SignatureException) {
            SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "토큰 서명이 유효하지 않습니다.")
            return
        } catch (e: Exception) {
            logger().error("JWT processing error", e)
            SecurityFilterResponseUtil.sendErrorResponse(response, objectMapper, "토큰 처리 중 오류가 발생했습니다.")
            return
        }

        filterChain.doFilter(request, response)
    }
}

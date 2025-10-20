package team.themoment.datagsm.global.security.jwt.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.security.jwt.JwtProvider

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()
    private val objectMapper = ObjectMapper()

    companion object {
        private val EXCLUDED_PATHS =
            listOf(
                "/v1/auth/google",
                "/v1/health",
                "/swagger-ui/**",
                "/api-docs/**",
            )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        EXCLUDED_PATHS.any { path ->
            pathMatcher.match(path, request.requestURI)
        }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val bearerToken = request.getHeader("Authorization")
        val token = jwtProvider.extractToken(bearerToken)

        if (token != null && jwtProvider.validateToken(token)) {
            val email = jwtProvider.getEmailFromToken(token)
            val role = jwtProvider.getRoleFromToken(token)

            val authentication =
                UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    listOf(role),
                )

            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
            return
        }

        val errorResponse = CommonApiResponse.error("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED)
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}

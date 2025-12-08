package team.themoment.datagsm.global.security.jwt.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.global.security.config.AuthenticationPathConfig
import team.themoment.datagsm.global.security.jwt.JwtProvider

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI
        return AuthenticationPathConfig.PUBLIC_PATHS.any { path ->
            pathMatcher.match(path, requestPath)
        }
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
        }
        filterChain.doFilter(request, response)
    }
}

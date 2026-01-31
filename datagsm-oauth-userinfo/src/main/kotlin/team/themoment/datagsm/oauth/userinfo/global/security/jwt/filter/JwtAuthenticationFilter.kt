package team.themoment.datagsm.oauth.userinfo.global.security.jwt.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.oauth.userinfo.global.security.jwt.JwtProvider

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
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
        val token = jwtProvider.extractToken(bearerToken)

        if (token != null) {
            jwtProvider.getAuthentication(token)?.let { authentication ->
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}

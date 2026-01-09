package team.themoment.datagsm.authorization.global.security.jwt.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import team.themoment.datagsm.authorization.global.authentication.CustomAuthenticationToken
import team.themoment.datagsm.authorization.global.security.authentication.principal.PrincipalProvider
import team.themoment.datagsm.authorization.global.security.config.AuthenticationPathConfig
import team.themoment.datagsm.authorization.global.security.jwt.JwtProvider
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.account.ApiScope

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val principalProvider: PrincipalProvider,
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
        val existingAuth = SecurityContextHolder.getContext().authentication
        if (existingAuth != null && existingAuth.isAuthenticated) {
            filterChain.doFilter(request, response)
            return
        }
        val bearerToken = request.getHeader("Authorization")
        val token = jwtProvider.extractToken(bearerToken)
        if (token != null && jwtProvider.validateToken(token)) {
            val role = jwtProvider.getRoleFromToken(token)
            val authorities =
                when (role) {
                    AccountRole.ADMIN, AccountRole.ROOT -> {
                        listOf(role, SimpleGrantedAuthority("SCOPE_${ApiScope.ALL_SCOPE}"))
                    }

                    AccountRole.USER -> {
                        listOf(role, SimpleGrantedAuthority("SCOPE_${ApiScope.AUTH_MANAGE.scope}"))
                    }

                    else -> {
                        listOf(role)
                    }
                }
            val authentication =
                CustomAuthenticationToken(
                    principal = principalProvider.provideFromJwt(token),
                    authorities = authorities,
                )

            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }
}

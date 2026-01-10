package team.themoment.datagsm.web.global.security.authentication.principal

import org.springframework.stereotype.Component
import team.themoment.datagsm.web.global.security.jwt.JwtProvider

@Component
class PrincipalProvider(
    val jwtProvider: JwtProvider,
) {
    fun provideFromJwt(validToken: String): CustomPrincipal {
        val email = jwtProvider.getEmailFromToken(validToken)
        return CustomPrincipal(
            email = email,
            type = jwtProvider.getAuthTypeFromToken(validToken),
            clientId = jwtProvider.getClientIdFromToken(validToken),
        )
    }
}

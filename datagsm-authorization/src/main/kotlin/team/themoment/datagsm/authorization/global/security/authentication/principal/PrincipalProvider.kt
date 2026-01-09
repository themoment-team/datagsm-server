package team.themoment.datagsm.authorization.global.security.authentication.principal

import org.springframework.stereotype.Component
import team.themoment.datagsm.authorization.global.security.jwt.JwtProvider

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

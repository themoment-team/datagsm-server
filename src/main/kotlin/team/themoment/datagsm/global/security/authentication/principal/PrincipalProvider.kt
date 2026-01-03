package team.themoment.datagsm.global.security.authentication.principal

import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.auth.entity.ApiKey
import team.themoment.datagsm.global.security.authentication.type.AuthType
import team.themoment.datagsm.global.security.jwt.JwtProvider

@Component
class PrincipalProvider(
    val jwtProvider: JwtProvider,
) {
    fun provideFromJwt(validToken: String): CustomPrincipal{
        val email = jwtProvider.getEmailFromToken(validToken)
        return CustomPrincipal(
            email = email,
            type = jwtProvider.getAuthTypeFromToken(validToken),
            clientId = jwtProvider.getClientIdFromToken(validToken),
            apiKey = null,
        )
    }
    fun provideFromApiKey(validApiKey: ApiKey): CustomPrincipal {
        return CustomPrincipal(
            email = validApiKey.account.email,
            type = AuthType.API_KEY,
            clientId = null,
            apiKey = validApiKey,
        )
    }
}

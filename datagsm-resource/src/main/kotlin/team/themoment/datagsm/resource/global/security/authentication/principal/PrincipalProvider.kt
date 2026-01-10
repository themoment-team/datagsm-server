package team.themoment.datagsm.resource.global.security.authentication.principal

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.resource.global.security.authentication.type.AuthType

@Component
class PrincipalProvider {
    fun provideFromApiKey(validApiKey: ApiKey): CustomPrincipal =
        CustomPrincipal(
            email = validApiKey.account.email,
            type = AuthType.API_KEY,
            clientId = null,
            apiKey = validApiKey,
        )
}

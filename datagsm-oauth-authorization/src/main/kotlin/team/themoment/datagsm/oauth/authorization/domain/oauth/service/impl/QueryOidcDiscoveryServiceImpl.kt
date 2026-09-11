package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.oauth.dto.response.OidcDiscoveryResDto
import team.themoment.datagsm.common.domain.oauth.entity.constant.GrantType
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryOidcDiscoveryService
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment

@Service
class QueryOidcDiscoveryServiceImpl(
    private val oauthEnvironment: OauthEnvironment,
    private val jwtEnvironment: OauthJwtProvisionEnvironment,
) : QueryOidcDiscoveryService {
    companion object {
        private const val SIGNING_ALGORITHM = "RS256"

        // sub는 모든 클라이언트에 같은 account.id를 내려주므로 public이다.
        private const val SUBJECT_TYPE_PUBLIC = "public"
    }

    // 지원 목록은 실제 구현에서 끌어온다. 손으로 적으면 grant_type이나 PKCE 방식이
    // 늘거나 줄었을 때 문서만 남아 SP가 지원하지 않는 값을 쓰게 된다.
    override fun execute(): OidcDiscoveryResDto {
        val issuer = oauthEnvironment.issuerUrl.trimEnd('/')
        val applicationId = jwtEnvironment.datagsmApplicationId

        return OidcDiscoveryResDto(
            issuer = issuer,
            authorizationEndpoint = "$issuer/v1/oauth/authorize",
            tokenEndpoint = "$issuer/v1/oauth/token",
            jwksUri = "$issuer/v1/oauth/jwks",
            userinfoEndpoint = oauthEnvironment.userinfoUrl,
            endSessionEndpoint = "$issuer/v1/oauth/logout",
            responseTypesSupported = listOf("code"),
            grantTypesSupported = GrantType.entries.map { it.value },
            subjectTypesSupported = listOf(SUBJECT_TYPE_PUBLIC),
            idTokenSigningAlgValuesSupported = listOf(SIGNING_ALGORITHM),
            codeChallengeMethodsSupported = PkceChallengeMethod.entries.map { it.value },
            scopesSupported =
                listOf(
                    OAuthScope.OPENID,
                    "$applicationId:${OAuthScope.ACCOUNT_READ}",
                    "$applicationId:${OAuthScope.STUDENT_READ}",
                    "$applicationId:${OAuthScope.CLUB_READ}",
                    "$applicationId:${OAuthScope.PROJECT_READ}",
                ),
            tokenEndpointAuthMethodsSupported = listOf("client_secret_post", "none"),
            claimsSupported = listOf("iss", "sub", "aud", "exp", "iat", "nonce", "email"),
        )
    }
}

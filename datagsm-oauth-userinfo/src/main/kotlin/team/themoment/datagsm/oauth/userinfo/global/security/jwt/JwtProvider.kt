package team.themoment.datagsm.oauth.userinfo.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.oauth.userinfo.global.data.OauthJwtVerificationEnvironment
import team.themoment.datagsm.oauth.userinfo.global.security.authentication.OauthAuthenticationToken
import team.themoment.datagsm.oauth.userinfo.global.security.authentication.principal.OauthUserPrincipal
import team.themoment.sdk.exception.ExpectedException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class JwtProvider(
    oauthJwtEnvironment: OauthJwtVerificationEnvironment,
) {
    private val publicKey: PublicKey = loadPublicKey(oauthJwtEnvironment.publicKey)

    fun extractToken(bearerToken: String?): String? =
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val email = claims.subject
        val clientId = getClientIdFromClaims(claims)
        val scopes = getScopesFromClaims(claims)
        return OauthAuthenticationToken(OauthUserPrincipal(email, clientId), scopes)
    }

    private fun getScopesFromClaims(claims: Claims): Set<OAuthScope> {
        val rawScopes =
            claims["scopes"] as? List<*>
                ?: throw ExpectedException("토큰에 scope 권한 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        return rawScopes
            .map { s ->
                val scopeStr = s as String
                OAuthScope.fromScopeString(scopeStr)
                    ?: throw ExpectedException("토큰에 잘못된 scope 권한 정보가 존재합니다.", HttpStatus.UNAUTHORIZED)
            }.toSet()
    }

    private fun getClientIdFromClaims(claims: Claims): String =
        claims["clientId"] as? String
            ?: throw ExpectedException("토큰에 클라이언트 아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private fun loadPublicKey(pem: String): PublicKey {
            val stripped =
                pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\\s".toRegex(), "")
            val decoded = Base64.getDecoder().decode(stripped)
            return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decoded))
        }
    }
}

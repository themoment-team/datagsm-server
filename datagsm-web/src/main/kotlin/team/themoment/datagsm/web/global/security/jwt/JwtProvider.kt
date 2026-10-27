package team.themoment.datagsm.web.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.web.global.data.OauthJwtVerificationEnvironment
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
    private val allowedClientIds: Set<String> = oauthJwtEnvironment.allowedClientIds.map { it.trim() }.toSet()

    fun isIssuedToAllowedClient(claims: Claims): Boolean = claims["clientId"] in allowedClientIds

    fun getEmail(claims: Claims): String = claims.subject

    fun getRole(claims: Claims): AccountRole {
        val roleName =
            claims["role"] as? String
                ?: throw ExpectedException("토큰에 역할 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        return AccountRole.valueOf(roleName)
    }

    fun extractToken(bearerToken: String?): String? =
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }

    // 서명 검증 비용이 크므로 요청마다 한 번만 파싱하고, 나머지 판단은 파싱된 클레임으로 한다.
    fun parseClaims(token: String): Claims =
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

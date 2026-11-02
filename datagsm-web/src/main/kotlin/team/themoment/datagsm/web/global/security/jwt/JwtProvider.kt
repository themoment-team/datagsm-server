package team.themoment.datagsm.web.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.web.global.data.OauthJwtVerificationEnvironment
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class JwtProvider(
    oauthJwtEnvironment: OauthJwtVerificationEnvironment,
) {
    private val publicKey: PublicKey = loadPublicKey(oauthJwtEnvironment.publicKey)
    private val allowedClientIds: Set<String> = oauthJwtEnvironment.allowedClientIdSet

    /**
     * 서명을 검증하고, web이 받아도 되는 access token인지까지 확인한 클레임을 돌려준다.
     * 검사를 호출측에 맡기면 다른 경로가 서명만 검증하고 지나칠 수 있어 한 곳에서 함께 수행한다.
     */
    fun parseAccessTokenClaims(token: String): Claims {
        val claims = parseClaims(token)
        val clientId = claims["clientId"] as? String

        // 다른 서비스가 받은 토큰에도 사용자의 role이 들어 있어, 출처를 확인하지 않으면 관리자 API까지 열린다.
        if (clientId == null || clientId !in allowedClientIds) {
            logger().warn("Rejected token issued to disallowed clientId {}", clientId)
            throw ExpectedException("이 서비스에서 사용할 수 없는 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }

        // refresh token도 같은 clientId를 가지므로, access token에만 기록되는 scopes로 종류를 구분한다.
        if (claims["scopes"] == null) {
            throw ExpectedException("이 서비스에서 사용할 수 없는 토큰입니다.", HttpStatus.UNAUTHORIZED)
        }
        return claims
    }

    fun getEmail(claims: Claims): String =
        claims.subject
            ?: throw ExpectedException("토큰에 사용자 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)

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

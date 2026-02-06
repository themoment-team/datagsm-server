package team.themoment.datagsm.oauth.userinfo.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment
import team.themoment.datagsm.oauth.userinfo.global.security.authentication.OauthAuthenticationToken
import team.themoment.datagsm.oauth.userinfo.global.security.authentication.principal.OauthUserPrincipal
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    oauthJwtEnvironment: OauthJwtEnvironment,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(
            oauthJwtEnvironment.secret.toByteArray(StandardCharsets.UTF_8),
        )

    fun extractToken(bearerToken: String?): String? =
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        if (claims.expiration?.before(Date()) == true) {
            throw ExpiredJwtException(null, claims, "JWT token has expired")
        }

        val scopes = getScopesFromClaims(claims)
        val email = claims.subject
        val clientId = getClientIdFromClaims(claims)

        return OauthAuthenticationToken(
            OauthUserPrincipal(email, clientId),
            scopes,
        )
    }

    private fun getScopesFromClaims(claims: Claims): Set<OAuthScope> {
        val rawScopes =
            claims["scopes"] as? List<*>
                ?: throw ExpectedException("토큰에 scope 권한 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        return rawScopes
            .map {
                runCatching { OAuthScope.valueOf(it as String) }
                    .getOrElse {
                        throw ExpectedException("토큰에 잘못된 scope 권한 정보가 존재합니다.", HttpStatus.UNAUTHORIZED)
                    }
            }.toSet()
    }

    private fun getClientIdFromClaims(claims: Claims): String =
        claims["clientId"] as? String ?: throw ExpectedException("토큰에 클라이언트 아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}

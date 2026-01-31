package team.themoment.datagsm.oauth.authorization.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.global.data.OauthJwtEnvironment
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtEnvironment: OauthJwtEnvironment,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(
            jwtEnvironment.secret.toByteArray(StandardCharsets.UTF_8),
        )

    fun generateOauthAccessToken(
        email: String,
        role: AccountRole,
        clientId: String,
        scopes: Set<OAuthScope>,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtEnvironment.accessTokenExpiration)

        return Jwts
            .builder()
            .subject(email)
            .claim("role", role.name)
            .claim("clientId", clientId)
            .claim("scopes", scopes)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun generateOauthRefreshToken(
        email: String,
        clientId: String,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtEnvironment.refreshTokenExpiration)

        return Jwts
            .builder()
            .subject(email)
            .claim("clientId", clientId)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean =
        try {
            val claims = parseClaims(token)
            claims.expiration?.after(Date()) ?: false
        } catch (e: Exception) {
            logger().error("Invalid JWT token ${e.message}")
            false
        }

    fun getEmailFromToken(token: String): String = parseClaims(token).subject

    fun getScopesFromToken(token: String): Set<OAuthScope> {
        val rawScopes =
            parseClaims(token)["scopes"] as? List<*>
                ?: throw ExpectedException("토큰에 scope 권한 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        val scopes =
            rawScopes
                .map {
                    runCatching { OAuthScope.valueOf(it as String) }
                        .getOrElse {
                            throw ExpectedException("토큰에 잘못된 scope 권한 정보가 존재합니다.", HttpStatus.UNAUTHORIZED)
                        }
                }.toSet()
        return scopes
    }

    fun getClientIdFromToken(token: String): String =
        parseClaims(token)["clientId"] as? String ?: throw ExpectedException("토큰에 클라이언트 아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)

    fun extractToken(bearerToken: String?): String? =
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}

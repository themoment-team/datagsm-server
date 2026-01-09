package team.themoment.datagsm.authorization.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.authorization.global.security.authentication.type.AuthType
import team.themoment.datagsm.common.global.data.JwtProperties
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.sdk.exception.ExpectedException
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val logger = LoggerFactory.getLogger(JwtProvider::class.java)

    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(
            jwtProperties.secret.toByteArray(StandardCharsets.UTF_8),
        )

    fun generateOauthAccessToken(
        email: String,
        role: AccountRole,
        clientId: String,
        scopes: Set<ApiScope>,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.oauthAccessTokenExpiration)

        return Jwts
            .builder()
            .subject(email)
            .claim("role", role.name)
            .claim("type", AuthType.OAUTH_JWT.name)
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
        val expiration = Date(now.time + jwtProperties.oauthRefreshTokenExpiration)

        return Jwts
            .builder()
            .subject(email)
            .claim("type", AuthType.OAUTH_JWT.name)
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
            logger.error("Invalid JWT token ${e.message}")
            false
        }

    fun getEmailFromToken(token: String): String = parseClaims(token).subject

    fun getRoleFromToken(token: String): AccountRole {
        val roleName =
            parseClaims(token)["role"] as? String
                ?: throw ExpectedException("토큰에 역할 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        return AccountRole.valueOf(roleName)
    }

    fun getAuthTypeFromToken(token: String): AuthType {
        val typeName =
            parseClaims(token)["type"] as? String
                ?: throw ExpectedException("토큰에 인증 타입이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        return AuthType.valueOf(typeName)
    }

    fun getClientIdFromToken(token: String): String? = parseClaims(token)["clientId"] as? String

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

package team.themoment.datagsm.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.auth.entity.constant.Role
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(
            jwtProperties.secret.toByteArray(StandardCharsets.UTF_8),
        )

    fun generateAccessToken(
        email: String,
        role: Role,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts
            .builder()
            .subject(email)
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(email: String): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.refreshTokenExpiration)

        return Jwts
            .builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            false
        }

    fun getEmailFromToken(token: String): String = parseClaims(token).subject

    fun getRoleFromToken(token: String): Role {
        val roleName = parseClaims(token)["role"] as String
        return Role.valueOf(roleName)
    }

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}

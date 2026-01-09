package team.themoment.datagsm.web.global.security.jwt

import com.github.snowykte0426.peanut.butter.logging.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.global.data.JwtEnvironment
import team.themoment.datagsm.web.global.security.authentication.type.AuthType
import team.themoment.sdk.exception.ExpectedException
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtEnvironment: JwtEnvironment,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(
            jwtEnvironment.secret.toByteArray(StandardCharsets.UTF_8),
        )

    fun generateAccessToken(
        email: String,
        role: AccountRole,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtEnvironment.accessTokenExpiration!!)

        return Jwts
            .builder()
            .subject(email)
            .claim("role", role.name)
            .claim("type", AuthType.INTERNAL_JWT.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(email: String): String {
        val now = Date()
        val expiration = Date(now.time + jwtEnvironment.refreshTokenExpiration!!)

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
            val claims = parseClaims(token)
            claims.expiration?.after(Date()) ?: false
        } catch (e: Exception) {
            logger().error("Invalid JWT token ${e.message}")
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

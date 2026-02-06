package team.themoment.datagsm.web.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.global.data.InternalJwtEnvironment
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    internalJwtEnvironment: InternalJwtEnvironment,
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(
            internalJwtEnvironment.secret.toByteArray(StandardCharsets.UTF_8),
        )

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

package team.themoment.datagsm.oauth.authorization.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.entity.constant.ThirdPartyScope
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtEnvironment
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.Date

@Component
class JwtProvider(
    private val jwtEnvironment: OauthJwtEnvironment,
) {
    private val privateKey: PrivateKey = loadPrivateKey(jwtEnvironment.privateKey)
    private val publicKey: PublicKey = loadPublicKey(jwtEnvironment.publicKey)
    private val keyId: String = jwtEnvironment.keyId

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
            .header()
            .keyId(keyId)
            .and()
            .subject(email)
            .claim("role", role.name)
            .claim("clientId", clientId)
            .claim("scopes", scopes.map { it.scope })
            .issuedAt(now)
            .expiration(expiration)
            .signWith(privateKey, Jwts.SIG.RS256)
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
            .header()
            .keyId(keyId)
            .and()
            .subject(email)
            .claim("clientId", clientId)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact()
    }

    fun generateClientCredentialsAccessToken(
        clientId: String,
        scopes: Set<OAuthScope>,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtEnvironment.accessTokenExpiration)

        return Jwts
            .builder()
            .header()
            .keyId(keyId)
            .and()
            .subject(clientId)
            .claim("clientId", clientId)
            .claim("scopes", scopes.map { it.scope })
            .claim("grant_type", "client_credentials")
            .issuedAt(now)
            .expiration(expiration)
            .signWith(privateKey, Jwts.SIG.RS256)
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
        return rawScopes
            .map { s ->
                val scopeStr = s as String
                OAuthScope.fromString(scopeStr)
                    ?: ThirdPartyScope.fromScopeString(scopeStr)
                    ?: throw ExpectedException("토큰에 잘못된 scope 권한 정보가 존재합니다.", HttpStatus.UNAUTHORIZED)
            }.toSet()
    }

    fun getClientIdFromToken(token: String): String =
        parseClaims(token)["clientId"] as? String
            ?: throw ExpectedException("토큰에 클라이언트 아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)

    fun extractToken(bearerToken: String?): String? =
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }

    fun getPublicKey(): PublicKey = publicKey

    fun getKeyId(): String = keyId

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private fun loadPrivateKey(pem: String): PrivateKey {
            val stripped =
                pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\\s".toRegex(), "")
            val decoded = Base64.getDecoder().decode(stripped)
            return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(decoded))
        }

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

package team.themoment.datagsm.oauth.authorization.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment
import team.themoment.datagsm.oauth.authorization.global.security.authentication.OauthAuthenticationToken
import team.themoment.datagsm.oauth.authorization.global.security.authentication.principal.OauthUserPrincipal
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
    private val jwtEnvironment: OauthJwtProvisionEnvironment,
    private val oauthEnvironment: OauthEnvironment,
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

    /**
     * OIDC ID Token을 발급한다.
     *
     * sub에는 email을 넣는다. OIDC는 sub가 발급자 내에서 영구적이고 재사용되지
     * 않는 값일 것을 요구하는데, 이 시스템의 계정 email은 학교 계정에 묶여
     * 변경되지 않으므로 그 조건을 만족한다.
     * access token과 /userinfo도 같은 값을 sub로 쓰고 있어, 세 곳의 식별자가
     * 어긋나지 않는 편이 SP 연동에서도 혼란이 없다.
     */
    fun generateIdToken(
        email: String,
        clientId: String,
        nonce: String?,
    ): String {
        val now = Date()
        val expiration = Date(now.time + jwtEnvironment.accessTokenExpiration)

        return Jwts
            .builder()
            .header()
            .keyId(keyId)
            .and()
            .issuer(oauthEnvironment.issuerUrl)
            .subject(email)
            .audience()
            .add(clientId)
            .and()
            .claim("email", email)
            .apply { nonce?.let { claim("nonce", it) } }
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

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val email = claims.subject
        val clientId =
            claims["clientId"] as? String
                ?: throw ExpectedException("토큰에 클라이언트 아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        val scopes = getScopesFromClaims(claims)
        return OauthAuthenticationToken(OauthUserPrincipal(email, clientId), scopes)
    }

    fun validateToken(token: String): Boolean =
        try {
            val claims = parseClaims(token)
            claims.expiration?.after(Date()) ?: false
        } catch (e: Exception) {
            logger().error("Caught exception during JWT token validation {}", e.message)
            false
        }

    fun getEmailFromToken(token: String): String = parseClaims(token).subject

    fun getScopesFromToken(token: String): Set<OAuthScope> = getScopesFromClaims(parseClaims(token))

    private fun getScopesFromClaims(claims: Claims): Set<OAuthScope> {
        val rawScopes =
            claims["scopes"] as? List<*>
                ?: throw ExpectedException("토큰에 권한 범위가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        return rawScopes
            .map { s ->
                val scopeStr = s as String
                OAuthScope.fromScopeString(scopeStr)
                    ?: throw ExpectedException("토큰에 잘못된 권한 범위가 존재합니다.", HttpStatus.UNAUTHORIZED)
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

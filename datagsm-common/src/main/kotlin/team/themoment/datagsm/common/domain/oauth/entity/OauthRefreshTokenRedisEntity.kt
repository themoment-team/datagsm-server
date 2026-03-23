package team.themoment.datagsm.common.domain.oauth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("oauthRefreshToken")
data class OauthRefreshTokenRedisEntity(
    @Id
    val id: String, // email:clientId 형식의 composite key
    @Indexed
    val email: String,
    @Indexed
    val clientId: String,
    val token: String,
    val scopes: Set<String>,
    @TimeToLive
    val ttl: Long,
) {
    companion object {
        fun of(
            email: String,
            clientId: String,
            token: String,
            scopes: Set<String>,
            ttl: Long,
        ): OauthRefreshTokenRedisEntity =
            OauthRefreshTokenRedisEntity(
                id = "$email:$clientId",
                email = email,
                clientId = clientId,
                token = token,
                scopes = scopes,
                ttl = ttl,
            )
    }
}

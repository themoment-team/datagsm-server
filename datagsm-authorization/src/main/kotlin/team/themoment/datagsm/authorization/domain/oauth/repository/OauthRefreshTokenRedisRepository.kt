package team.themoment.datagsm.authorization.domain.oauth.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.OauthRefreshTokenRedisEntity
import java.util.Optional

interface OauthRefreshTokenRedisRepository : CrudRepository<OauthRefreshTokenRedisEntity, String> {
    fun findByEmailAndClientId(
        email: String,
        clientId: String,
    ): Optional<OauthRefreshTokenRedisEntity>

    fun deleteByEmailAndClientId(
        email: String,
        clientId: String,
    )
}

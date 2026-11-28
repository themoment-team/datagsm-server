package team.themoment.datagsm.common.domain.oauth.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthRefreshTokenRedisEntity
import java.util.Optional

interface OauthRefreshTokenRedisRepository : CrudRepository<OauthRefreshTokenRedisEntity, String> {
    fun findByEmailAndClientId(
        email: String,
        clientId: String,
    ): Optional<OauthRefreshTokenRedisEntity>

    // Redis 리포지토리는 deleteBy 파생 쿼리를 삭제로 실행하지 않고 조회만 한다.
    // 단건 삭제는 OauthRefreshTokenRedisEntity.idOf로 만든 id와 deleteById를 사용한다.

    fun findAllByEmail(email: String): List<OauthRefreshTokenRedisEntity>
}

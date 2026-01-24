package team.themoment.datagsm.common.domain.auth.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.entity.RefreshTokenRedisEntity
import java.util.Optional

interface RefreshTokenRedisRepository : CrudRepository<RefreshTokenRedisEntity, String> {
    fun findByEmail(email: String): Optional<RefreshTokenRedisEntity>

    fun deleteByEmail(email: String)
}

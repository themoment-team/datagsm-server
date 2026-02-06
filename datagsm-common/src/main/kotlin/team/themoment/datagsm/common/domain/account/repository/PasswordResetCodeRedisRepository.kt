package team.themoment.datagsm.common.domain.account.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity

interface PasswordResetCodeRedisRepository : CrudRepository<PasswordResetCodeRedisEntity, String>

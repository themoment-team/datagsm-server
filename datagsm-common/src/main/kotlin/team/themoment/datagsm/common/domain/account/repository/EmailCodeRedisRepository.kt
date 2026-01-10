package team.themoment.datagsm.common.domain.account.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity

interface EmailCodeRedisRepository : CrudRepository<EmailCodeRedisEntity, String>

package team.themoment.datagsm.domain.account.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.EmailCodeRedisEntity

interface EmailCodeRedisRepository : CrudRepository<EmailCodeRedisEntity, String>

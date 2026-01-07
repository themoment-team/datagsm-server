package team.themoment.datagsm.authorization.domain.account.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.EmailCodeRedisEntity

interface EmailCodeRedisRepository : CrudRepository<EmailCodeRedisEntity, String>

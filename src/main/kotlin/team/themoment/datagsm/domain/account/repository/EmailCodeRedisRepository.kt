package team.themoment.datagsm.domain.account.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.domain.account.entity.EmailCodeRedisEntity

interface EmailCodeRedisRepository : CrudRepository<EmailCodeRedisEntity, String>

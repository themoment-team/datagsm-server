package team.themoment.datagsm.common.domain.oauth.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.oauth.entity.IdpSessionRedisEntity

interface IdpSessionRedisRepository : CrudRepository<IdpSessionRedisEntity, String>

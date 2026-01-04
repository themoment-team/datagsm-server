package team.themoment.datagsm.domain.oauth.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.domain.oauth.entity.OauthCodeRedisEntity

interface OauthCodeRedisRepository : CrudRepository<OauthCodeRedisEntity, String>

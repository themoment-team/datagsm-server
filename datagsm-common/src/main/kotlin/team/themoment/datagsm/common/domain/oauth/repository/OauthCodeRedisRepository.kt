package team.themoment.datagsm.common.domain.oauth.repository

import org.springframework.data.repository.CrudRepository
import team.themoment.datagsm.common.domain.account.OauthCodeRedisEntity

interface OauthCodeRedisRepository : CrudRepository<OauthCodeRedisEntity, String>

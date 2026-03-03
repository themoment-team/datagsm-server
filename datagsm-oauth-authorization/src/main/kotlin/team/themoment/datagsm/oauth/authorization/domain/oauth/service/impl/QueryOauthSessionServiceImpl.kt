package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthSessionResDto
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryOauthSessionService

@Service
class QueryOauthSessionServiceImpl(
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val clientJpaRepository: ClientJpaRepository,
) : QueryOauthSessionService {
    @Transactional(readOnly = true)
    override fun execute(token: String): OauthSessionResDto {
        val stateEntity =
            oauthAuthorizeStateRedisRepository
                .findByIdOrNull(token)
                ?: throw OAuthException.InvalidRequest("유효하지 않거나 만료된 세션입니다.")
        val client =
            clientJpaRepository
                .findByIdOrNull(stateEntity.clientId)
                ?: throw OAuthException.InvalidClient("존재하지 않는 클라이언트입니다.")
        return OauthSessionResDto(serviceName = client.serviceName)
    }
}

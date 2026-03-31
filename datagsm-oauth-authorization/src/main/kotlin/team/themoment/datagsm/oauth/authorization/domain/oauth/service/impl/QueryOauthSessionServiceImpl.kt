package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.application.repository.ThirdPartyScopeJpaRepository
import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeResDto
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthSessionResDto
import team.themoment.datagsm.common.domain.oauth.repository.OauthAuthorizeStateRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryOauthSessionService
import team.themoment.sdk.exception.ExpectedException
import java.time.Instant

@Service
class QueryOauthSessionServiceImpl(
    private val oauthAuthorizeStateRedisRepository: OauthAuthorizeStateRedisRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val oauthEnvironment: OauthEnvironment,
    private val thirdPartyScopeJpaRepository: ThirdPartyScopeJpaRepository,
) : QueryOauthSessionService {
    // OAuth 모듈이지만 OAuthException을 사용하지 않는 이유는 해당 API는 공개적으로 공인된 API가 아니며 서비스 내부에서만 사용되기 때문입니다.
    // 따라서 일반적인 인증 실패로 간주하여 ExpectedException을 사용합니다.
    @Transactional(readOnly = true)
    override fun execute(token: String): OauthSessionResDto {
        val stateEntity =
            oauthAuthorizeStateRedisRepository
                .findByIdOrNull(token)
                ?: throw ExpectedException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED)
        val client =
            clientJpaRepository
                .findByIdOrNull(stateEntity.clientId)
                ?: throw ExpectedException("유효하지 않은 클라이언트입니다.", HttpStatus.UNAUTHORIZED)
        val expiresAt = Instant.now().toEpochMilli() + oauthEnvironment.authorizeStateExpirationMs
        val requestedScopes = resolveScopes(stateEntity.scopes)
        return OauthSessionResDto(serviceName = client.serviceName, expiresAt = expiresAt, requestedScopes = requestedScopes)
    }

    private fun resolveScopes(scopeStrings: Set<String>): List<OAuthScopeResDto> {
        val builtinScopes = scopeStrings.mapNotNull { OAuthScope.fromString(it) }
        val thirdPartyStrings = scopeStrings.subtract(builtinScopes.map { it.scope }.toSet())

        val thirdPartyScopes =
            if (thirdPartyStrings.isNotEmpty()) {
                val appIds = thirdPartyStrings.map { it.substringBefore(':') }.toSet()
                val fetched =
                    thirdPartyScopeJpaRepository
                        .findAllByApplicationIdIn(appIds)
                        .associateBy { "${it.application.id}:${it.scopeName}" }
                thirdPartyStrings
                    .mapNotNull { fetched[it] }
                    .map { OAuthScopeResDto(scope = "${it.application.id}:${it.scopeName}", description = it.description) }
            } else {
                emptyList()
            }

        return builtinScopes.map { OAuthScopeResDto(scope = it.scope, description = it.description) } + thirdPartyScopes
    }
}

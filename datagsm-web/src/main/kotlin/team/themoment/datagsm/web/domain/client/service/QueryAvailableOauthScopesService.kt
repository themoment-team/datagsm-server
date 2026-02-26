package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeGroupListResDto

interface QueryAvailableOauthScopesService {
    fun execute(): OAuthScopeGroupListResDto
}

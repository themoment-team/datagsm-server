package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.domain.client.dto.response.OAuthScopeListResDto

interface QueryAvailableOauthScopesService {
    fun execute(): OAuthScopeListResDto
}

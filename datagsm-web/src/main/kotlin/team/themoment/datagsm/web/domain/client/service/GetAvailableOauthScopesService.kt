package team.themoment.datagsm.web.domain.client.service

import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeGroupListResDto

interface GetAvailableOauthScopesService {
    fun execute(): ApiScopeGroupListResDto
}

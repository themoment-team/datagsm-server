package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.common.dto.auth.response.ApiKeyResDto

interface QueryCurrentAccountApiKeyService {
    fun execute(): ApiKeyResDto
}

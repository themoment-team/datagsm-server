package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.resource.domain.auth.dto.response.ApiKeyResDto

interface QueryCurrentAccountApiKeyService {
    fun execute(): ApiKeyResDto
}

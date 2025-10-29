package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto

interface QueryApiKeyService {
    fun execute(): ApiKeyResDto
}

package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto

interface ModifyApiKeyService {
    fun execute(): ApiKeyResDto
}

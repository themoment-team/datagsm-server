package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.ApiKeyResDto

interface CreateApiKeyService {
    fun execute(authorization: String): ApiKeyResDto
}
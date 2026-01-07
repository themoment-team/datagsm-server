package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.web.domain.auth.dto.response.ApiKeyResDto

interface QueryCurrentAccountApiKeyService {
    fun execute(): ApiKeyResDto
}

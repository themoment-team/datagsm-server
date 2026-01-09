package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.dto.auth.response.ApiKeyResDto

interface QueryCurrentAccountApiKeyService {
    fun execute(): ApiKeyResDto
}

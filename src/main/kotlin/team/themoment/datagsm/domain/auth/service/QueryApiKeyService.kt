package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.MaskedApiKeyResDto

interface QueryApiKeyService {
    fun execute(): MaskedApiKeyResDto
}

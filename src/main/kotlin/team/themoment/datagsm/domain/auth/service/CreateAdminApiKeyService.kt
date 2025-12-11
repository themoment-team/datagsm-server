package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto

interface CreateAdminApiKeyService {
    fun execute(reqDto: CreateApiKeyReqDto): ApiKeyResDto
}
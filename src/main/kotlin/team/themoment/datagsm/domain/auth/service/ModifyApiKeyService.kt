package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.domain.auth.dto.response.ApiKeyResDto

interface ModifyApiKeyService {
    fun execute(reqDto: ModifyApiKeyReqDto): ApiKeyResDto
}

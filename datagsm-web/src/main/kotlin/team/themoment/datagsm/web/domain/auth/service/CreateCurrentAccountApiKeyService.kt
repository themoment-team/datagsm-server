package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.web.domain.auth.dto.request.CreateApiKeyReqDto
import team.themoment.datagsm.web.domain.auth.dto.response.ApiKeyResDto

interface CreateCurrentAccountApiKeyService {
    fun execute(reqDto: CreateApiKeyReqDto): ApiKeyResDto
}

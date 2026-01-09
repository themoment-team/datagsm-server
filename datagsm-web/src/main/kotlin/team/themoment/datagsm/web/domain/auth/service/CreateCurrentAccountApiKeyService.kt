package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.dto.auth.request.CreateApiKeyReqDto
import team.themoment.datagsm.common.dto.auth.response.ApiKeyResDto

interface CreateCurrentAccountApiKeyService {
    fun execute(reqDto: CreateApiKeyReqDto): ApiKeyResDto
}

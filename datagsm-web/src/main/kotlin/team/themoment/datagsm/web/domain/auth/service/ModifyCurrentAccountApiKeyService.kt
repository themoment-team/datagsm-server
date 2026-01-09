package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.domain.auth.dto.request.ModifyApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeyResDto

interface ModifyCurrentAccountApiKeyService {
    fun execute(reqDto: ModifyApiKeyReqDto): ApiKeyResDto
}

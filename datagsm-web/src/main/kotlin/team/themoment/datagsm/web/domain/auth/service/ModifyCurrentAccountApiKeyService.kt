package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.dto.auth.request.ModifyApiKeyReqDto
import team.themoment.datagsm.common.dto.auth.response.ApiKeyResDto

interface ModifyCurrentAccountApiKeyService {
    fun execute(reqDto: ModifyApiKeyReqDto): ApiKeyResDto
}

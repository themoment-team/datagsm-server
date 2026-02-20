package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.domain.auth.dto.request.SearchApiKeyReqDto
import team.themoment.datagsm.common.domain.auth.dto.response.ApiKeySearchResDto

interface SearchApiKeyService {
    fun execute(searchReq: SearchApiKeyReqDto): ApiKeySearchResDto
}

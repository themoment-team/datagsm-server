package team.themoment.datagsm.web.domain.application.service

import team.themoment.datagsm.common.domain.application.dto.request.SearchApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationListResDto

interface SearchApplicationService {
    fun execute(queryReq: SearchApplicationReqDto): ApplicationListResDto
}

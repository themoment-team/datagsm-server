package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.QueryAccountReqDto
import team.themoment.datagsm.common.domain.account.dto.response.AccountListResDto

interface QueryAccountService {
    fun execute(queryReq: QueryAccountReqDto): AccountListResDto
}

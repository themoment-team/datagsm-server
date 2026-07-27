package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.response.AccountResDto

interface QueryAccountDetailService {
    fun execute(accountId: Long): AccountResDto
}

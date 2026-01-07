package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.web.domain.account.dto.request.CreateAccountReqDto

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

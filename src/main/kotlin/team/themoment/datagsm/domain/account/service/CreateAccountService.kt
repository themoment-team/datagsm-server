package team.themoment.datagsm.domain.account.service

import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.domain.account.dto.request.CreateAccountReqDto

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

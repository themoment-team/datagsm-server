package team.themoment.datagsm.authorization.domain.account.service

import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.dto.account.request.CreateAccountReqDto

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

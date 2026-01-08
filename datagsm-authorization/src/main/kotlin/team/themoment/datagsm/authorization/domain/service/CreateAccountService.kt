package team.themoment.datagsm.authorization.domain.account.service

import team.themoment.datagsm.authorization.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.AccountJpaEntity

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

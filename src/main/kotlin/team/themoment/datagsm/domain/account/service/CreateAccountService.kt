package team.themoment.datagsm.domain.account.service

import team.themoment.datagsm.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

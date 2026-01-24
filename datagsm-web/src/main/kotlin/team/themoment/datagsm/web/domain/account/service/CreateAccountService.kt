package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.dto.account.request.CreateAccountReqDto

interface CreateAccountService {
    fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity
}

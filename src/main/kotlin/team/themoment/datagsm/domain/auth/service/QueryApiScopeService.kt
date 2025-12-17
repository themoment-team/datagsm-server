package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto

interface QueryApiScopeService {
    fun execute(role: AccountRole): List<ApiScopeResDto>
}

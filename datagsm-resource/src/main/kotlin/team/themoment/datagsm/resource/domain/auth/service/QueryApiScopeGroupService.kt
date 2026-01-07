package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.resource.domain.auth.dto.response.ApiScopeGroupListResDto

interface QueryApiScopeGroupService {
    fun execute(role: AccountRole): ApiScopeGroupListResDto
}

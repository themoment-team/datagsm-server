package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.dto.auth.response.ApiScopeGroupListResDto

interface QueryApiScopeGroupService {
    fun execute(role: AccountRole): ApiScopeGroupListResDto
}

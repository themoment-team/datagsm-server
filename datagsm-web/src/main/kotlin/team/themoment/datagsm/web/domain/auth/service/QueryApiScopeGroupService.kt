package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeGroupListResDto

interface QueryApiScopeGroupService {
    fun execute(role: AccountRole): ApiScopeGroupListResDto
}

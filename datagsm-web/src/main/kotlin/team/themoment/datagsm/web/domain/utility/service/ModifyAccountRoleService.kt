package team.themoment.datagsm.web.domain.utility.service

import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.sdk.response.CommonApiResponse

interface ModifyAccountRoleService {
    fun execute(
        email: String,
        role: AccountRole,
    ): CommonApiResponse<Nothing>
}

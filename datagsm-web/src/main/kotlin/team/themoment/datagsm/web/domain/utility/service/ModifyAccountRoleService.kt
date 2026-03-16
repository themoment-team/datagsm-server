package team.themoment.datagsm.web.domain.utility.service

import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole

interface ModifyAccountRoleService {
    fun execute(
        email: String,
        role: AccountRole,
    )
}

package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.ModifyAccountRoleReqDto

interface ModifyAccountRoleService {
    fun execute(
        accountId: Long,
        reqDto: ModifyAccountRoleReqDto,
    )
}

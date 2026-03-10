package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.DeleteMyAccountReqDto

interface DeleteMyAccountService {
    fun execute(reqDto: DeleteMyAccountReqDto)
}

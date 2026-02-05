package team.themoment.datagsm.oauth.authorization.domain.account.password.service

import team.themoment.datagsm.common.domain.account.dto.request.ChangePasswordReqDto

interface ModifyPasswordService {
    fun execute(reqDto: ChangePasswordReqDto)
}

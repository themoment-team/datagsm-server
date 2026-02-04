package team.themoment.datagsm.oauth.authorization.domain.password.service

import team.themoment.datagsm.common.domain.account.dto.request.ChangePasswordReqDto

interface ModifyPasswordService {
    fun execute(reqDto: ChangePasswordReqDto)
}

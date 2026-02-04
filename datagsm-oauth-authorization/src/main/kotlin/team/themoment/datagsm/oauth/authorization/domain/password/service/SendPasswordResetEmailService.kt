package team.themoment.datagsm.oauth.authorization.domain.password.service

import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto

interface SendPasswordResetEmailService {
    fun execute(reqDto: SendPasswordResetEmailReqDto)
}

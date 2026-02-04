package team.themoment.datagsm.oauth.authorization.domain.account.password.service

import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto

interface SendPasswordResetEmailService {
    fun execute(reqDto: SendPasswordResetEmailReqDto)
}

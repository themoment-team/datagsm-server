package team.themoment.datagsm.oauth.authorization.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto

interface SendSignupEmailService {
    fun execute(reqDto: SendEmailReqDto)
}

package team.themoment.datagsm.authorization.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto

interface SendEmailService {
    fun execute(reqDto: SendEmailReqDto)
}

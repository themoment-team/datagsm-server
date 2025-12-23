package team.themoment.datagsm.domain.account.service

import team.themoment.datagsm.domain.account.dto.request.SendEmailReqDto

interface SendEmailService {
    fun execute(reqDto: SendEmailReqDto)
}

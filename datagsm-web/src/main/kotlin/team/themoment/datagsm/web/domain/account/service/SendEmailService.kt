package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto

interface SendEmailService {
    fun execute(reqDto: SendEmailReqDto)
}

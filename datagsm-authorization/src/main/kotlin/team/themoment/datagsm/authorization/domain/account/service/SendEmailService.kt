package team.themoment.datagsm.authorization.domain.account.service

import team.themoment.datagsm.common.dto.account.request.SendEmailReqDto

interface SendEmailService {
    fun execute(reqDto: SendEmailReqDto)
}

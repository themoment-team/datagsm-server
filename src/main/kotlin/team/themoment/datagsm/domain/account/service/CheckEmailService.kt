package team.themoment.datagsm.domain.account.service

import team.themoment.datagsm.domain.account.dto.request.EmailCodeReqDto

interface CheckEmailService {
    fun execute(reqDto: EmailCodeReqDto)
}

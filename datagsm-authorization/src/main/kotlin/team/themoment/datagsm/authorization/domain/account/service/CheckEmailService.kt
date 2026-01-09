package team.themoment.datagsm.authorization.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto

interface CheckEmailService {
    fun execute(reqDto: EmailCodeReqDto)
}

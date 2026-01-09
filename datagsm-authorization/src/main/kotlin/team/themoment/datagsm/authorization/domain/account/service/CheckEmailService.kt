package team.themoment.datagsm.authorization.domain.account.service

import team.themoment.datagsm.common.dto.account.request.EmailCodeReqDto

interface CheckEmailService {
    fun execute(reqDto: EmailCodeReqDto)
}

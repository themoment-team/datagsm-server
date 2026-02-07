package team.themoment.datagsm.oauth.authorization.domain.account.service

import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto

interface CheckSignupEmailService {
    fun execute(reqDto: EmailCodeReqDto)
}

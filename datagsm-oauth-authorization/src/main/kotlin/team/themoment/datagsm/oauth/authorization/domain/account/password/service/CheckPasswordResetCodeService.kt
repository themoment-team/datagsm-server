package team.themoment.datagsm.oauth.authorization.domain.account.password.service

import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto

interface CheckPasswordResetCodeService {
    fun execute(reqDto: VerifyPasswordResetCodeReqDto)
}

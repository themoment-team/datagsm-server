package team.themoment.datagsm.oauth.authorization.domain.password.service

import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto

interface CheckPasswordResetCodeService {
    fun execute(reqDto: VerifyPasswordResetCodeReqDto)
}

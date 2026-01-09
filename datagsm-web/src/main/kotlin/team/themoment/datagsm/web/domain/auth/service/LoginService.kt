package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.dto.auth.request.LoginReqDto
import team.themoment.datagsm.common.dto.auth.response.TokenResDto

interface LoginService {
    fun execute(reqDto: LoginReqDto): TokenResDto
}

package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.web.domain.auth.dto.request.LoginReqDto
import team.themoment.datagsm.web.domain.auth.dto.response.TokenResDto

interface LoginService {
    fun execute(reqDto: LoginReqDto): TokenResDto
}

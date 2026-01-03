package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.request.LoginReqDto
import team.themoment.datagsm.domain.auth.dto.response.TokenResDto

interface LoginService {
    fun execute(reqDto: LoginReqDto): TokenResDto
}

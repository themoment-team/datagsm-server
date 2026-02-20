package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.Oauth2TokenResDto

interface Oauth2TokenService {
    fun execute(reqDto: Oauth2TokenReqDto): Oauth2TokenResDto
}

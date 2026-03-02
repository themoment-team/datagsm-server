package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.response.OauthSessionResDto

interface QueryOauthSessionService {
    fun execute(token: String): OauthSessionResDto
}

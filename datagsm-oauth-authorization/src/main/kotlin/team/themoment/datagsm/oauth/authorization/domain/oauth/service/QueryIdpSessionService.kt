package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.response.IdpSessionListResDto

interface QueryIdpSessionService {
    fun execute(sessionId: String?): IdpSessionListResDto
}

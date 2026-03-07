package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.response.JwkSetResDto

interface QueryJwkSetService {
    fun execute(): JwkSetResDto
}

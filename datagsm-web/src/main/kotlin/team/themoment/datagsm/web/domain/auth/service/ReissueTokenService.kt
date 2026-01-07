package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.web.domain.auth.dto.response.TokenResDto

interface ReissueTokenService {
    fun execute(refreshToken: String): TokenResDto
}

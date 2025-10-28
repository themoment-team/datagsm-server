package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.TokenResDto

interface ReissueTokenService {
    fun execute(refreshToken: String): TokenResDto
}

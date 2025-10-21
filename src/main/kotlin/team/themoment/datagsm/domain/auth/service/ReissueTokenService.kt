package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.TokenResDto

interface ReissueTokenService {
    fun execute(refreshToken: String): TokenResDto
}

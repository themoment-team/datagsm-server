package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.dto.auth.response.TokenResDto

interface ReissueTokenService {
    fun execute(refreshToken: String): TokenResDto
}

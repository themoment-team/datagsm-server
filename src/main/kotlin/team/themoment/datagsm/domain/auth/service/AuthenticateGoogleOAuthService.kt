package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.TokenResDto

interface AuthenticateGoogleOAuthService {
    fun execute(authorizationCode: String): TokenResDto
}
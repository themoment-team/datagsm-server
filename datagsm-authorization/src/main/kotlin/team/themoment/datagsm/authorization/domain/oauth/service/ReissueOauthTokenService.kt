package team.themoment.datagsm.authorization.domain.oauth.service

import team.themoment.datagsm.authorization.domain.oauth.dto.response.OauthTokenResDto

interface ReissueOauthTokenService {
    fun execute(refreshToken: String): OauthTokenResDto
}

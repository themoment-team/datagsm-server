package team.themoment.datagsm.domain.oauth.service

import team.themoment.datagsm.domain.oauth.dto.response.OauthTokenResDto

interface ReissueOauthTokenService {
    fun execute(refreshToken: String): OauthTokenResDto
}

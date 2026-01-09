package team.themoment.datagsm.authorization.domain.oauth.service

import team.themoment.datagsm.common.dto.oauth.response.OauthTokenResDto

interface ReissueOauthTokenService {
    fun execute(refreshToken: String): OauthTokenResDto
}

package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.response.OauthTokenResDto

interface ReissueOauthTokenService {
    fun execute(refreshToken: String): OauthTokenResDto
}

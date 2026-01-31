package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.request.OauthTokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthTokenResDto

interface ExchangeTokenService {
    fun execute(reqDto: OauthTokenReqDto): OauthTokenResDto
}

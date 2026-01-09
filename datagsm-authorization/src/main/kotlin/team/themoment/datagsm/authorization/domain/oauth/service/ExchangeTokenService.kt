package team.themoment.datagsm.authorization.domain.oauth.service

import team.themoment.datagsm.common.dto.oauth.request.OauthTokenReqDto
import team.themoment.datagsm.common.dto.oauth.response.OauthTokenResDto

interface ExchangeTokenService {
    fun execute(reqDto: OauthTokenReqDto): OauthTokenResDto
}

package team.themoment.datagsm.domain.oauth.service

import team.themoment.datagsm.domain.oauth.dto.request.OauthTokenReqDto
import team.themoment.datagsm.domain.oauth.dto.response.OauthTokenResDto

interface ExchangeTokenService {
    fun execute(reqDto: OauthTokenReqDto): OauthTokenResDto
}

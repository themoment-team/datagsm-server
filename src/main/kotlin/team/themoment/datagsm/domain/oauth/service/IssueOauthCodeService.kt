package team.themoment.datagsm.domain.oauth.service

import team.themoment.datagsm.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.domain.oauth.dto.response.OauthCodeResDto

interface IssueOauthCodeService {
    fun execute(reqDto: OauthCodeReqDto): OauthCodeResDto
}

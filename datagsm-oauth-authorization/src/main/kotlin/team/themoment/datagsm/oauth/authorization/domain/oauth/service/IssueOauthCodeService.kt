package team.themoment.datagsm.oauth.authorization.domain.oauth.service

import team.themoment.datagsm.common.domain.oauth.dto.request.OauthCodeReqDto
import team.themoment.datagsm.common.domain.oauth.dto.response.OauthCodeResDto

interface IssueOauthCodeService {
    fun execute(reqDto: OauthCodeReqDto): OauthCodeResDto
}

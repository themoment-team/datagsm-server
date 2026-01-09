package team.themoment.datagsm.authorization.domain.oauth.service

import team.themoment.datagsm.common.dto.oauth.request.OauthCodeReqDto
import team.themoment.datagsm.common.dto.oauth.response.OauthCodeResDto

interface IssueOauthCodeService {
    fun execute(reqDto: OauthCodeReqDto): OauthCodeResDto
}

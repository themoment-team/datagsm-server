package team.themoment.datagsm.userinfo.domain.userinfo.service

import team.themoment.datagsm.common.domain.account.dto.response.GetMyInfoResDto

interface GetUserInfoService {
    fun execute(): GetMyInfoResDto
}

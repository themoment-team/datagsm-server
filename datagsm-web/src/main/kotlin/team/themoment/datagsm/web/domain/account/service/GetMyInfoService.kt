package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.common.dto.account.response.GetMyInfoResDto

interface GetMyInfoService {
    fun execute(): GetMyInfoResDto
}

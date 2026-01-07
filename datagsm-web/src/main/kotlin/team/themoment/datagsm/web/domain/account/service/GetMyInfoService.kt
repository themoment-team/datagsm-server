package team.themoment.datagsm.web.domain.account.service

import team.themoment.datagsm.web.domain.account.dto.response.GetMyInfoResDto

interface GetMyInfoService {
    fun execute(): GetMyInfoResDto
}

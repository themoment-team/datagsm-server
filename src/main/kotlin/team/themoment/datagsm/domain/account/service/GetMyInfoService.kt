package team.themoment.datagsm.domain.account.service

import team.themoment.datagsm.domain.account.dto.response.GetMyInfoResDto

interface GetMyInfoService {
    fun execute(): GetMyInfoResDto
}

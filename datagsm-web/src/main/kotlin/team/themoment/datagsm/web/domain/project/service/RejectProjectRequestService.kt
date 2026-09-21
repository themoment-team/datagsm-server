package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.RejectProjectRequestReqDto

interface RejectProjectRequestService {
    fun execute(
        requestId: Long,
        reqDto: RejectProjectRequestReqDto,
    )
}

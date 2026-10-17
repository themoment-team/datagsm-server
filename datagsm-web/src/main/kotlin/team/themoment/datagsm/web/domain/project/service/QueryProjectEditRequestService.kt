package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectEditRequestReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestListResDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto

interface QueryProjectEditRequestService {
    fun execute(queryReq: QueryProjectEditRequestReqDto): ProjectEditRequestListResDto

    fun executeById(requestId: Long): ProjectEditRequestResDto
}

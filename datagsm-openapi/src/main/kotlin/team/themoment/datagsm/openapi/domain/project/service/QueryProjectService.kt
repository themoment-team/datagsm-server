package team.themoment.datagsm.openapi.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.QueryProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectListResDto

interface QueryProjectService {
    fun execute(queryReq: QueryProjectReqDto): ProjectListResDto
}

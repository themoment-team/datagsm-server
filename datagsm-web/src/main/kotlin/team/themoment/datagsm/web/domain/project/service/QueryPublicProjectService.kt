package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.QueryPublicProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.PublicProjectListResDto
import team.themoment.datagsm.common.domain.project.dto.response.PublicProjectResDto

interface QueryPublicProjectService {
    fun execute(queryReq: QueryPublicProjectReqDto): PublicProjectListResDto

    fun executeById(projectId: Long): PublicProjectResDto
}

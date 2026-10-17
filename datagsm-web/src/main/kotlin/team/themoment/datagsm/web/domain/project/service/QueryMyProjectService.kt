package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.QueryMyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.MyProjectListResDto

interface QueryMyProjectService {
    fun execute(queryReq: QueryMyProjectReqDto): MyProjectListResDto
}

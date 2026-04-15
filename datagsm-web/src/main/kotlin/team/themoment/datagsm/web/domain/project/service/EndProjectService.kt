package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.EndProjectReqDto

interface EndProjectService {
    fun execute(
        projectId: Long,
        reqDto: EndProjectReqDto,
    )
}

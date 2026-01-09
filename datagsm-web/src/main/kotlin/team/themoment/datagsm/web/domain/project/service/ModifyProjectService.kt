package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.dto.project.request.ProjectReqDto
import team.themoment.datagsm.common.dto.project.response.ProjectResDto

interface ModifyProjectService {
    fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto
}

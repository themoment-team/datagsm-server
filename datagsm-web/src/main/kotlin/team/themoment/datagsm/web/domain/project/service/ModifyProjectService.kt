package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.web.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.web.domain.project.dto.response.ProjectResDto

interface ModifyProjectService {
    fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto
}

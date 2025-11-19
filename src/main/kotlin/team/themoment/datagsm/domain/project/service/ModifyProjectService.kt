package team.themoment.datagsm.domain.project.service

import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.dto.response.ProjectResDto

interface ModifyProjectService {
    fun execute(
        projectId: Long,
        reqDto: ProjectReqDto,
    ): ProjectResDto
}

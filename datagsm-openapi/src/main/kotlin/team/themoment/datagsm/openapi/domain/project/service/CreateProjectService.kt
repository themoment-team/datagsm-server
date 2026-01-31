package team.themoment.datagsm.openapi.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto

interface CreateProjectService {
    fun execute(projectReqDto: ProjectReqDto): ProjectResDto
}

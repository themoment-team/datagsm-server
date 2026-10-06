package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectEditRequestResDto

interface ApplyProjectModificationService {
    fun execute(
        projectId: Long,
        reqDto: ApplyProjectReqDto,
    ): ProjectEditRequestResDto
}

package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto

interface AcceptProjectRequestService {
    fun execute(requestId: Long): ProjectResDto
}

package team.themoment.datagsm.web.domain.project.service

import team.themoment.datagsm.common.domain.project.dto.request.CreateProjectIconUploadUrlReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectIconUploadUrlResDto

interface CreateProjectIconUploadUrlService {
    fun execute(reqDto: CreateProjectIconUploadUrlReqDto): ProjectIconUploadUrlResDto
}

package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.web.domain.student.dto.request.UpdateMyGithubIdReqDto

interface ModifyMyGithubIdService {
    fun execute(reqDto: UpdateMyGithubIdReqDto)
}

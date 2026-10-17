package team.themoment.datagsm.web.domain.student.service

import team.themoment.datagsm.common.domain.student.dto.response.ParticipantCandidateListResDto

interface QueryParticipantCandidateService {
    fun execute(): ParticipantCandidateListResDto
}

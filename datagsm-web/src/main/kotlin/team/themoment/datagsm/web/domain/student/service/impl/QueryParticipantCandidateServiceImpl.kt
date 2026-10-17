package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.student.dto.response.ParticipantCandidateListResDto
import team.themoment.datagsm.common.domain.student.dto.response.ParticipantCandidateResDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.QueryParticipantCandidateService

@Service
class QueryParticipantCandidateServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : QueryParticipantCandidateService {
    @Transactional(readOnly = true)
    override fun execute(): ParticipantCandidateListResDto =
        ParticipantCandidateListResDto(
            students = studentJpaRepository.findAllEnrolledStudents().map { ParticipantCandidateResDto.from(it) },
        )
}

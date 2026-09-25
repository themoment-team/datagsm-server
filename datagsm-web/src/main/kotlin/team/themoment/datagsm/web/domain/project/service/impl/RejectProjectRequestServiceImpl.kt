package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.project.dto.request.RejectProjectRequestReqDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.ProjectEditRequestJpaRepository
import team.themoment.datagsm.web.domain.project.service.RejectProjectRequestService
import team.themoment.sdk.exception.ExpectedException
import java.time.LocalDateTime

@Service
class RejectProjectRequestServiceImpl(
    private val projectEditRequestJpaRepository: ProjectEditRequestJpaRepository,
) : RejectProjectRequestService {
    @Transactional
    override fun execute(
        requestId: Long,
        reqDto: RejectProjectRequestReqDto,
    ) {
        val request =
            projectEditRequestJpaRepository
                .findById(requestId)
                .orElseThrow { ExpectedException("신청 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }

        if (request.requestStatus != ProjectRequestStatus.PENDING) {
            throw ExpectedException("이미 처리된 신청입니다.", HttpStatus.CONFLICT)
        }

        request.requestStatus = ProjectRequestStatus.REJECTED
        request.rejectReason = reqDto.reason
        request.processedAt = LocalDateTime.now()
        projectEditRequestJpaRepository.save(request)
    }
}

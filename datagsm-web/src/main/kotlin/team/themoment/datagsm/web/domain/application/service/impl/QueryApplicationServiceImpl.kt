package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.QueryApplicationService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryApplicationServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
) : QueryApplicationService {
    @Transactional(readOnly = true)
    override fun execute(id: String): ApplicationResDto {
        val application =
            applicationJpaRepository.findById(id).orElseThrow {
                ExpectedException("Application을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }
        return application.toResDto()
    }
}

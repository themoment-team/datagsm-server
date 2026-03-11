package team.themoment.datagsm.common.domain.application.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity

interface ApplicationJpaCustomRepository {
    fun searchApplicationWithPaging(
        name: String?,
        id: String?,
        pageable: Pageable,
    ): Page<ApplicationJpaEntity>

    fun findAllWithThirdPartyScope(): List<ApplicationJpaEntity>
}

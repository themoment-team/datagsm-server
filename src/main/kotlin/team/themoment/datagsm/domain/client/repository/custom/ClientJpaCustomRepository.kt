package team.themoment.datagsm.domain.client.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.client.ClientJpaEntity

interface ClientJpaCustomRepository {
    fun searchClientWithPaging(
        name: String?,
        pageable: Pageable,
    ): Page<ClientJpaEntity>
}

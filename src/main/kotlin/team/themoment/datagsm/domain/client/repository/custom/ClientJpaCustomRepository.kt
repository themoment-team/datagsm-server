package team.themoment.datagsm.domain.client.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity

interface ClientJpaCustomRepository {
    fun searchClientWithPaging(
        name: String?,
        pageable: Pageable,
    ): Page<ClientJpaEntity>
}
